package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.EntitySpawnEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.interact.PlaceInteraction;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.inventory.SwapHandler;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.combat.crystal.CrystalCalcManager;
import net.shoreline.client.impl.module.combat.crystal.CrystalData;
import net.shoreline.client.impl.module.combat.crystal.CrystalOptimizer;
import net.shoreline.client.impl.module.combat.util.DamageUtil;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
import net.shoreline.client.impl.module.impl.Priorities;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.network.NetworkUtil;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.RotateMode;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.math.PerSecond;
import net.shoreline.client.util.math.QueueAverage;
import net.shoreline.client.util.world.WorldUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class AutoCrystalModule extends ObsidianPlacerModule
{
    public static AutoCrystalModule INSTANCE;

    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while interacting")
            .setDefaultValue(true).build();
    Config<Boolean> swingConfig = new BooleanConfig.Builder("Swing")
            .setDescription("Swings the hand when attacking")
            .setDefaultValue(true).build();

    Config<Float> targetRange = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(15.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to target entities").build();
    Config<Integer> extrapolateTicks = new NumberConfig.Builder<Integer>("Extrapolate")
            .setMin(0).setDefaultValue(0).setMax(10).setFormat(" ticks")
            .setDescription("The number of ticks ahead to predict movement").build();
    Config<Boolean> targetNakeds = new BooleanConfig.Builder("Nakeds")
            .setDescription("Targets nakeds").setVisible(targetPlayers::getValue).setDefaultValue(true).build();
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetRange, extrapolateTicks, targetPlayers, targetNakeds, targetHostiles, targetPassives).build();

    Config<Float> breakRange = new NumberConfig.Builder<Float>("BreakRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to break crystals").build();
    Config<Float> breakTrace = new NumberConfig.Builder<Float>("BreakTrace")
            .setMin(0.0f).setMax(6.0f).setDefaultValue(3.0f).setFormat("m")
            .setDescription("The range to break crystals through walls").build();
    Config<Integer> breakDelay = new NumberConfig.Builder<Integer>("BreakDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between breaking crystals").build();
    Config<Integer> ticksExisted = new NumberConfig.Builder<Integer>("MinExisted")
            .setMin(0).setMax(10).setDefaultValue(0).setFormat(" ticks")
            .setDescription("The minimum ticks existed before breaking crystals").build();
    Config<Void> breakConfig = new ConfigGroup.Builder("Break")
            .addAll(breakRange, breakTrace, breakDelay, ticksExisted).build();

    Config<Float> placeRange = new NumberConfig.Builder<Float>("PlaceRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to place crystals").build();
    Config<Float> placeTrace = new NumberConfig.Builder<Float>("PlaceTrace")
            .setMin(0.0f).setMax(6.0f).setDefaultValue(3.0f).setFormat("m")
            .setDescription("The range to place crystals through walls").build();
    Config<Integer> placeLimit = new NumberConfig.Builder<Integer>("PlaceLimit")
            .setMin(1).setMax(10).setDefaultValue(2)
            .setDescription("The limit of crystal placements per tick").build();
    Config<Boolean> protocolPlace = new BooleanConfig.Builder("Protocol")
            .setDescription("Prevents placements in 1x1 areas")
            .setDefaultValue(false).build();
    Config<Boolean> basePlace = new BooleanConfig.Builder("Support")
            .setDescription("Places an obsidian block if there is none")
            .setDefaultValue(false).build();
    Config<Void> placeConfig = new ConfigGroup.Builder("Place")
            .addAll(placeRange, placeTrace, placeLimit, protocolPlace, basePlace).build();

    Config<Boolean> sequentialBreak = new BooleanConfig.Builder("InstantBreak")
            .setDescription("Breaks immediately after a placement")
            .setDefaultValue(false).build();
    Config<Boolean> sequentialPlace = new BooleanConfig.Builder("InstantPlace")
            .setDescription("Places immediately after breaking a crystal")
            .setDefaultValue(false).build();
    Config<Boolean> predictAttack = new BooleanConfig.Builder("PredictAttack")
            .setDescription("Attempts to predict the next attack (works better on low ping)")
            .setDefaultValue(false).build();
    Config<Void> sequentialConfig = new ConfigGroup.Builder("Sequential")
            .addAll(sequentialBreak, sequentialPlace, predictAttack).build();

    Config<Timing> predictPlace = new EnumConfig.Builder<Timing>("PredictPlace")
            .setValues(Timing.values())
            .setDescription("Attempts to predict the next place")
            .setDefaultValue(Timing.OFF).build();
    Config<Boolean> cevBreak = new BooleanConfig.Builder("CevBreak")
            .setDescription("Targets crystal placements above the target")
            .setDefaultValue(false).build();
    Config<Boolean> targetItems = new BooleanConfig.Builder("TargetItems")
            .setDescription("Targets dropped items blocking placements")
            .setDefaultValue(false).build();
    Config<Integer> prePlace = new NumberConfig.Builder<Integer>("PrePlace")
            .setMin(0).setMax(10).setDefaultValue(5).setFormat(" ticks")
            .setDescription("Ticks before predicting placement")
            .setVisible(() -> targetItems.getValue()).build();
    Config<Void> antiSurroundConfig = new ConfigGroup.Builder("SurroundBreak")
            .addAll(predictPlace, cevBreak, targetItems, prePlace).build();

    Config<Float> minDamage = new NumberConfig.Builder<Float>("MinDamage")
            .setMin(2.0f).setMax(10.0f).setDefaultValue(4.0f)
            .setDescription("The minimum damage to consider crystals").build();
    Config<Float> maxSelfDamage = new NumberConfig.Builder<Float>("MaxSelfDamage")
            .setMin(2.0f).setMax(20.0f).setDefaultValue(12.0f)
            .setDescription("The maximum damage a crystal can do to the player").build();
    Config<Boolean> overrideConfig = new BooleanConfig.Builder("Override")
            .setDescription("Allows overriding minimum damage (e.g. allows crystal spam)")
            .setDefaultValue(true).build();
    Config<Float> armorMultiplier = new NumberConfig.Builder<Float>("ArmorMultiplier")
            .setMin(1.0f).setMax(5.0f).setDefaultValue(1.0f).setFormat("x")
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("The minimum armor damage to consider spamming crystals").build();
    Config<Float> damageMultiplier = new NumberConfig.Builder<Float>("DamageMultiplier")
            .setMin(1.0f).setMax(5.0f).setDefaultValue(1.0f).setFormat("x")
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("Place if we can kill target in this many crystals").build();
    Config<Boolean> ignoreTerrain = new BooleanConfig.Builder("IgnoreTerrain")
            .setDescription("Ignores explodable terrain during damage calculations")
            .setDefaultValue(false).build();
    Config<Void> damageConfig = new ConfigGroup.Builder("Damage")
            .addAll(minDamage, maxSelfDamage, overrideConfig, armorMultiplier,
                    damageMultiplier, ignoreTerrain).build();

    Config<RotateMode> rotateConfig = new EnumConfig.Builder<RotateMode>("Rotate")
            .setValues(RotateMode.values())
            .setDescription("Rotates to before interacting")
            .setDefaultValue(RotateMode.OFF).build();

    Config<Boolean> autoSwap = new BooleanConfig.Builder("AutoSwap")
            .setDescription("Automatically swaps to crystals before placing")
            .setDefaultValue(false).build();
    Config<Boolean> swapBack = new BooleanConfig.Builder("SwapBack")
            .setVisibilityDependant(true)
            .setDescription("Swaps back to your previously held slot")
            .setVisible(() -> autoSwap.getValue())
            .setDefaultValue(false).build();
    Config<Boolean> silentSwap = new BooleanConfig.Builder("SilentSwap")
            .setVisibilityDependant(true)
            .setDescription("Silently swaps to crystals before placing")
            .setVisible(() -> autoSwap.getValue())
            .setDefaultValue(false).build();
    Config<Boolean> antiWeakness = new BooleanConfig.Builder("AntiWeakness")
            .setVisibilityDependant(true)
            .setDescription("Swaps to sword before attacking crystals")
            .setVisible(() -> autoSwap.getValue() && silentSwap.getValue())
            .setDefaultValue(false).build();
    Config<SilentSwapType> silentType = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The silent swap type")
            .setVisible(() -> autoSwap.getValue() && silentSwap.getValue())
            .setDefaultValue(SilentSwapType.HOTBAR).build();
    Config<Void> swapConfig = new ConfigGroup.Builder("Swap")
            .addAll(autoSwap, silentSwap, antiWeakness, silentType).build();

    private final CrystalCalcManager crystalCalc;

    private static final Box FULL_CRYSTAL_BB = new Box(-0.5, 0.0, -0.5, 0.5, 2.0, 0.5);
    private static final Box HALF_CRYSTAL_BB = new Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5);

    private final CrystalOptimizer optimizer = new CrystalOptimizer();

    private CrystalData<EntityState> currentAttack;
    private CrystalData<BlockPos> currentPlace;

    private final SwapHandler autoSwapHandler = new SwapHandler();
    private final Timer attackTimer = new NanoTimer();
    private final Timer attackSpeedTimer = new NanoTimer();

    private final ConcurrentMap<Integer, Long> attackPackets = new ConcurrentHashMap<>();

    private final ConcurrentMap<PlaceInteraction, Long> placePackets = new ConcurrentHashMap<>();
    private final AtomicInteger crystalsPlaced = new AtomicInteger();

    private boolean silentRotated;

    private final QueueAverage breakTime = new QueueAverage(20, 1000L);
    private final PerSecond cps = new PerSecond();

    private final ConcurrentMap<BlockPos, CrystalData<BlockPos>> fadeAnimations = new ConcurrentHashMap<>();

    private long highestId;

    public AutoCrystalModule()
    {
        super("AutoCrystal", new String[] {"CrystalAura"}, "Best CA on the market", GuiCategory.COMBAT);
        this.crystalCalc = new CrystalCalcManager(this);
        INSTANCE = this;
    }

    @Override
    public void onDisable()
    {
        currentAttack = null;
        currentPlace = null;
        attackPackets.clear();
        placePackets.clear();
        silentRotated = false;
    }

    @Override
    public String getModuleData()
    {
        if (!isRunning())
        {
            return super.getModuleData();
        }

        return String.format("%sms, %s", DECIMAL.format(breakTime.average()), cps.getPerSecond());
    }

    @EventListener(priority = Priorities.AUTO_CRYSTAL)
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || mc.player.isSpectator())
        {
            crystalCalc.cancelRun();
            currentAttack = null;
            currentPlace = null;
            return;
        }

        List<CrystalData<BlockPos>> latestCrystalBases = crystalCalc.getBaseResults();
        List<CrystalData<EntityState>> latestCrystalEntities = crystalCalc.getEntityResults();

        currentAttack = getBestCrystal(latestCrystalEntities);

        List<CrystalData<BlockPos>> placements = crystalCalc.getBasePlacements();
        currentPlace = getBestCrystal(basePlace.getValue() && placements.isEmpty() ? latestCrystalBases : placements);
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }
        
        crystalsPlaced.set(0);
        crystalCalc.runCalc();
    }

    @EventListener(priority = Priorities.AUTO_CRYSTAL)
    public void onClientRotation(ClientRotationEvent event)
    {
        if (checkNull() || event.isCanceled() || mc.player.isSpectator())
        {
            return;
        }

        float[] rotations = null;
        silentRotated = false;

        final Hand hand = getCrystalHand();
        if (currentAttack != null)
        {
            rotations = runAttack(currentAttack, hand);
        }

        if (currentPlace != null)
        {
            rotations = runPlace(currentPlace, hand);
        } else if (SpeedMineModule.INSTANCE.isUsedByAutoMine() && predictPlace.getValue() != Timing.OFF)
        {
            MiningData currentMine = SpeedMineModule.INSTANCE.getMainMiningBlock();
            CrystalData.Immediate<BlockPos> prePlaceData = validateMiningData(currentMine);
            if (prePlaceData != null)
            {
                currentPlace = prePlaceData;
                rotations = runPlaceInternal(prePlaceData, hand);
            }
        }

        if (silentRotated)
        {
            Managers.ROTATION.resetSilentRotation();
            return;
        }

        if (rotations != null && rotateConfig.getValue() == RotateMode.NORMAL)
        {
            event.cancel();
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);
        }
    }

    @EventListener
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        if (checkNull() || event.getType() != EntityType.END_CRYSTAL)
        {
            return;
        }

        Vec3d crystalPos = event.getPos();
        BlockPos crystalBase = BlockPos.ofFloored(crystalPos.offset(Direction.DOWN, 1.0));

        if (!placePackets.keySet().removeIf(d -> d.getPos().equals(crystalBase)))
        {
            return;
        }

        Hand hand = getCrystalHand();
        if (sequentialBreak.getValue())
        {
            attackCrystal(event.getEntityId(), hand);
            attackTimer.reset();
        }

        if (currentPlace != null && sequentialPlace.getValue())
        {
            placeCrystal(currentPlace.getValue(), currentPlace.getCrystalVec(), hand);
        }

        if (predictAttack.getValue())
        {
            int nextAttackId = (int) (highestId + 1);
            Entity entity = mc.world.getEntityById(nextAttackId);
            if (entity == null)
            {
                attackCrystal(nextAttackId, hand);
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                Long time = attackPackets.remove(id);
                if (time != null)
                {
                    cps.count();
                    breakTime.add(System.currentTimeMillis() - time);
                }
            }
        }

        if (event.getPacket() instanceof ExperienceOrbSpawnS2CPacket packet && packet.getEntityId() > highestId)
        {
            highestId = packet.getEntityId();
        }

        if (event.getPacket() instanceof EntitySpawnS2CPacket packet && packet.getEntityId() > highestId)
        {
            highestId = packet.getEntityId();
        }

        if (checkNull() || predictPlace.getValue() != Timing.INSTANT || currentPlace != null)
        {
            return;
        }

        MiningData currentMine = SpeedMineModule.INSTANCE.getMainMiningBlock();
        if (currentMine == null)
        {
            return;
        }

        Hand hand = getCrystalHand();

        if (!AutoMineModule.INSTANCE.isEnabled() || !SpeedMineModule.INSTANCE.isEnabled())
        {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && packet.getState().isAir())
        {
            CrystalData.Immediate<BlockPos> prePlace = validateMiningData(currentMine);
            if (prePlace == null)
            {
                return;
            }

            if (packet.getPos().equals(prePlace.getValue().up()))
            {
                runPlaceInternal(prePlace, hand);
            }
        }

        if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                Entity entity = mc.world.getEntityById(id);
                if (entity instanceof ItemEntity)
                {
                    BlockPos pos = entity.getBlockPos();
                    CrystalData.Immediate<BlockPos> prePlace = validateMiningData(currentMine);
                    if (prePlace == null)
                    {
                        continue;
                    }

                    if (pos.equals(prePlace.getValue().up()))
                    {
                        runPlaceInternal(prePlace, hand);
                        return;
                    }
                }
            }
        }

        if (event.getPacket() instanceof ItemPickupAnimationS2CPacket packet)
        {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity instanceof ItemEntity)
            {
                BlockPos pos = entity.getBlockPos();
                CrystalData.Immediate<BlockPos> prePlace = validateMiningData(currentMine);
                if (prePlace == null)
                {
                    return;
                }

                if (pos.equals(prePlace.getValue().up()))
                {
                    runPlaceInternal(prePlace, hand);
                }
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (currentPlace != null)
        {
            fadeAnimations.put(currentPlace.getValue(), currentPlace);
        }

        for (Map.Entry<BlockPos, CrystalData<BlockPos>> entry : fadeAnimations.entrySet())
        {
            BlockPos placePos = entry.getKey();
            CrystalData<BlockPos> placeData = entry.getValue();
            Animation anim = placeData.getAnimation();

            if (anim.getFactor() <= 0.01)
            {
                fadeAnimations.remove(placePos);
                continue;
            }

            double animFactor = Easing.SMOOTH_STEP.ease(anim.getFactor());
            if (currentPlace == null || !currentPlace.getValue().equals(placePos))
            {
                anim.setState(false);
            }

            BoxRender.FILL.render(event.getMatrixStack(),
                    placePos, ThemeModule.INSTANCE.getPrimaryColor().getRGB(), (float) animFactor);

            String dataNametag = DECIMAL.format(placeData.getDamageToTarget());
            if (placeData instanceof CrystalData.Immediate<?> immediate)
            {
                String dataTag = immediate.getTag();
                dataNametag = dataTag != null ? dataTag : dataNametag + "x";
            }

            Managers.RENDER.renderNametag(event.getMatrixStack(),
                    placePos.toCenterPos(),
                    0.003f,
                    dataNametag,
                    ColorUtil.withTransparency(Colors.WHITE, (float) animFactor));
        }
    }

    private float[] runAttack(CrystalData<EntityState> attack, Hand hand)
    {
        EntityState crystalState = attack.getValue();
        Vec3d crystalVec = crystalState.getPos().add(0.0, 0.5, 0.0);
        float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalVec);
        if (rotateConfig.getValue() == RotateMode.SILENT && !silentRotated)
        {
            Managers.ROTATION.setSilentRotation(new Rotation(rotations[0], rotations[1]));
            silentRotated = true;
        }

        if (breakDelay.getValue() == 0 || attackTimer.hasPassed(breakDelay.getValue()))
        {
            attackCrystal(crystalState.getId(), hand);
            attackTimer.reset();
        }

        return rotations;
    }

    private float[] runPlace(CrystalData<BlockPos> placement, Hand hand)
    {
        BlockPos crystalPos = placement.getValue();
        if (basePlace.getValue() && !canUseOnBlock(crystalPos))
        {
            if (!runSingleObbyPlacement(crystalPos))
            {
                currentPlace = null;
                return null;
            }
        }

        return runPlaceInternal(placement, hand);
    }

    private float[] runPlaceInternal(CrystalData<BlockPos> placement, Hand hand)
    {
        BlockPos crystalPos = placement.getValue();
        Vec3d crystalVec = crystalPos.toBottomCenterPos().add(0.0, 1.5, 0.0);
        float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalVec);
        if (rotateConfig.getValue() == RotateMode.SILENT && !silentRotated)
        {
            Managers.ROTATION.setSilentRotation(new Rotation(rotations[0], rotations[1]));
            silentRotated = true;
        }

        placeCrystal(crystalPos, crystalVec, hand);
        return rotations;
    }

    private void attackCrystal(int crystalId, Hand hand)
    {
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);

        boolean canBreakCrystal = weakness == null || (strength != null && strength.getAmplifier() >= weakness.getAmplifier());
        if (!canBreakCrystal && antiWeakness.getValue())
        {
            int slot = getAntiWeaknessSlot();
            if (slot == -1 || !Managers.INVENTORY.startSwap(slot, silentType.getValue()))
            {
                return;
            }
        }

        sendAttackPacketsInternal(crystalId, swingConfig.getValue(), hand);
        optimizer.setDead(crystalId);

        if (!canBreakCrystal)
        {
            Managers.INVENTORY.endSwap(silentType.getValue());
        }

        attackPackets.put(crystalId, System.currentTimeMillis());
    }

    private void placeCrystal(BlockPos blockPos, Vec3d crystalVec, Hand hand)
    {
        int slot = InventoryUtil.getItemSlot(Items.END_CRYSTAL, silentType.getValue());
        if (slot == -1)
        {
            return;
        }

        if (crystalsPlaced.get() > placeLimit.getValue())
        {
            return;
        }

        if (currentAttack == null && crystalVec != null)
        {
            Box placeArea = FULL_CRYSTAL_BB.offset(crystalVec);

            List<EndCrystalEntity> blocking = WorldUtil.collectEntitiesInBox(EndCrystalEntity.class,
                    placeArea,
                    e -> ExplosionUtil.crystalDamageToEntity(mc.world, mc.player, crystalVec) <= maxSelfDamage.getValue());

            if (!blocking.isEmpty())
            {
                attackCrystal(blocking.getFirst().getId(), hand);
                attackTimer.reset();
            }
        }

        Box baseBox = new Box(blockPos);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d cut = new Vec3d(MathHelper.clamp(eyePos.getX(), baseBox.minX, baseBox.maxX),
                MathHelper.clamp(eyePos.getY(), baseBox.minY, baseBox.maxY),
                MathHelper.clamp(eyePos.getZ(), baseBox.minZ, baseBox.maxZ));

        Direction placeDir = getPlaceDirection(blockPos, baseBox, eyePos, cut);
        BlockHitResult result = new BlockHitResult(cut, placeDir, blockPos, baseBox.contains(eyePos));

        if (silentSwap.getValue())
        {
            if (!Managers.INVENTORY.startSwap(slot, silentType.getValue()))
            {
                return;
            }

        } else if (autoSwap.getValue())
        {
            autoSwapHandler.handleSwaps();
            if (autoSwapHandler.canAutoSwap())
            {
                Managers.INVENTORY.setSelectedSlot(slot);
            }
        }

        PlaceInteraction placeInteraction = PlaceInteraction.builder()
                .pos(blockPos)
                .direction(placeDir)
                .build();

        if (Managers.INVENTORY.isHolding(Items.END_CRYSTAL, hand))
        {
            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
            if (swingConfig.getValue())
            {
                mc.player.swingHand(hand);
            } else
            {
                sendPacket(new HandSwingC2SPacket(hand));
            }
        }

        if (silentSwap.getValue())
        {
            Managers.INVENTORY.endSwap(silentType.getValue());
        }

        placePackets.put(placeInteraction, System.currentTimeMillis());
        crystalsPlaced.incrementAndGet();
    }

    private Direction getPlaceDirection(BlockPos blockPos, Box box, Vec3d eyePos, Vec3d cut)
    {
        if (eyePos.y >= box.maxY)
        {
            return Direction.UP;
        } else if (blockPos.getY() >= mc.world.getTopYInclusive())
        {
            return Direction.DOWN;
        }

        return Direction.getFacing(eyePos.x - cut.x, eyePos.y - cut.y, eyePos.z - cut.z);
    }

    private <T> CrystalData<T> getBestCrystal(List<CrystalData<T>> crystals)
    {
        CrystalData<T> bestCrystal = getBestCrystal(crystals, false);
        if (bestCrystal == null || bestCrystal.getDamageToTarget() < minDamage.getValue())
        {
            return getBestCrystal(crystals, true);
        }

        return bestCrystal;
    }

    public <T extends CrystalData<?>> T getBestCrystal(List<T> crystals, boolean onlyImmediate)
    {
        if (crystals.isEmpty())
        {
            return null;
        }

        T bestCrystal = null;
        double bestDamage = 0.0f;
        for (T data : crystals)
        {
            if (onlyImmediate && !(data instanceof CrystalData.Immediate<?>))
            {
                continue;
            }

            CrystalData<?> candidate = validateCrystalData((CrystalData<?>) data, bestDamage);
            if (candidate == null)
            {
                continue;
            }

            bestDamage = candidate.getDamageToTarget();
            bestCrystal = data;
        }

        return bestCrystal;
    }

    private <T> CrystalData<T> validateCrystalData(CrystalData<T> data, double currentBest)
    {
        LivingEntityState state = data.getTarget();
        if (state == null || state.isDead())
        {
            return null;
        }

        Entity entity = state.getEntity();
        if (!(entity instanceof LivingEntity target) || target.isDead())
        {
            return null;
        }

        float baseDamage = (float) data.getDamageToTarget();
        float baseSelfDamage = (float) data.getDamageToPlayer();

        if (baseDamage <= 0.0f || baseSelfDamage > maxSelfDamage.getValue())
        {
            return null;
        }

        float selfDamage = ExplosionUtil.getAppliedDamageToEntity(mc.player, baseSelfDamage);
        if (selfDamage > maxSelfDamage.getValue() || DamageUtil.getHealth(mc.player) - selfDamage < 0.5f)
        {
            return null;
        }

        float targetDamage = ExplosionUtil.getAppliedDamageToEntity(target, baseDamage);
        if (targetDamage > currentBest)
        {
            data.setDamageToTarget(targetDamage);
            data.setDamageToPlayer(selfDamage);

            return data;
        }

        return null;
    }

    private CrystalData.Immediate<BlockPos> validateMiningData(MiningData currentMine)
    {
        if (currentMine == null || !currentMine.isDoneMining())
        {
            return null;
        }

        PlayerEntity target = Managers.TARGETING.getTarget();
        if (target == null)
        {
            return null;
        }

        BlockPos minePos = currentMine.getBlockPos();
        BlockPos placePos = minePos.down();
        double dist = mc.player.squaredDistanceTo(placePos.toCenterPos());
        if (dist > MathHelper.square(placeRange.getValue()))
        {
            return null;
        }

        BlockState state = mc.world.getBlockState(placePos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return null;
        }

        if (hasEntityBlockingCrystal(getCrystalBox(minePos), true))
        {
            return null;
        }

        float selfDamage = (float) ExplosionUtil.crystalDamageToEntity(mc.world,
                mc.player,
                minePos.toBottomCenterPos(),
                true,
                Set.of(minePos));

        if (selfDamage > maxSelfDamage.getValue() || DamageUtil.getHealth(mc.player) - selfDamage < 0.5f)
        {
            return null;
        }

        float damage = (float) ExplosionUtil.crystalDamageToEntity(mc.world,
                target,
                minePos.toBottomCenterPos(),
                true,
                Set.of(minePos));

        if (damage < minDamage.getValue())
        {
            return null;
        }

        LivingEntityState targetState = new LivingEntityState(target);
        Vec3d crystalVec = minePos.toBottomCenterPos();
        return new CrystalData.Immediate<>("AS",
                placePos,
                crystalVec,
                targetState,
                damage,
                selfDamage);
    }

    public boolean canUseOnBlock(BlockPos blockPos)
    {
        return canUseOnBlock(mc.world, blockPos) && !hasEntityBlockingCrystal(getCrystalBox(blockPos.up()), false);
    }

    public boolean canUseOnBlock(BlockView blockView, BlockPos pos)
    {
        BlockState state = blockView.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return false;
        }

        BlockPos p2 = pos.up();
        BlockState state2 = blockView.getBlockState(p2);
        if (protocolPlace.getValue() && !blockView.getBlockState(p2.up()).isAir())
        {
            return false;
        }

        if (!state2.isAir() && !state2.isOf(Blocks.FIRE))
        {
            return false;
        }

        return true;
    }

    public boolean hasEntityBlockingCrystal(Box box, boolean ignoreItems)
    {
        for (Entity entity : WorldUtil.collectEntitiesInBox(box))
        {
            if (!canIgnoreEntity(entity.getType(), ignoreItems))
            {
                return true;
            }
        }

        return false;
    }

    public boolean canIgnoreEntity(EntityType<?> entity, boolean ignoreItems)
    {
        return entity == EntityType.EXPERIENCE_ORB || entity == EntityType.END_CRYSTAL || ignoreItems && entity == EntityType.ITEM;
    }

    public Box getCrystalBox(BlockPos blockPos)
    {
        Box crystalBB = NetworkUtil.getServerIp().contains("crystalpvp.cc") ? HALF_CRYSTAL_BB : FULL_CRYSTAL_BB;
        return crystalBB.offset(blockPos.toBottomCenterPos());
    }

    private Hand getCrystalHand()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.OFF_HAND;
        }

        return Hand.MAIN_HAND;
    }

    private int getAntiWeaknessSlot()
    {
        return InventoryUtil.getItemSlot((ItemStack itemStack) ->
                itemStack.getItem().getTranslationKey().contains("sword")
                        || itemStack.getItem().getTranslationKey().contains("axe")).getSlot();
    }

    @Override
    public boolean checkNull()
    {
        return super.checkNull() || (mc.player.isUsingItem() && !multitaskConfig.getValue());
    }

    public boolean isRunning()
    {
        return isEnabled() && (currentAttack != null || currentPlace != null);
    }

    private enum Timing
    {
        TICK, INSTANT, OFF
    }
}
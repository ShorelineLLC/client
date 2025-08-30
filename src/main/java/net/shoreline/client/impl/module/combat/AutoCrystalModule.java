package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
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
import net.shoreline.client.impl.event.TickPriorities;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.EntitySpawnEvent;
import net.shoreline.client.impl.event.network.ExplosionEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.combat.crystal.CrystalData;
import net.shoreline.client.impl.module.impl.CombatModule;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.RotateMode;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.math.PerSecond;
import net.shoreline.client.util.math.QueueAverage;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    Config<Boolean> targetPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Targets players").setDefaultValue(true).build();
    Config<Boolean> targetNakeds = new BooleanConfig.Builder("Nakeds")
            .setDescription("Targets nakeds").setVisible(targetPlayers::getValue).setDefaultValue(true).build();
    Config<Boolean> targetHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Targets hostiles").setDefaultValue(false).build();
    Config<Boolean> targetPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Targets passives").setDefaultValue(false).build();
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetRange, targetPlayers, targetNakeds, targetHostiles, targetPassives).build();

    Config<Float> breakRange = new NumberConfig.Builder<Float>("BreakRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to break crystals").build();
    Config<Integer> breakDelay = new NumberConfig.Builder<Integer>("BreakDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between breaking crystals").build();
    Config<Integer> ticksExisted = new NumberConfig.Builder<Integer>("TicksExisted")
            .setMin(0).setMax(10).setDefaultValue(0)
            .setDescription("The minimum ticks existed before breaking crystals").build();
    Config<Boolean> sequentialBreak = new BooleanConfig.Builder("SequentialBreak")
            .setDescription("Breaks immediately after a placement")
            .setDefaultValue(false).build();
    Config<Void> breakConfig = new ConfigGroup.Builder("Break")
            .addAll(breakRange, breakDelay, ticksExisted, sequentialBreak).build();

    Config<Float> placeRange = new NumberConfig.Builder<Float>("PlaceRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to place crystals").build();
    Config<Integer> placeDelay = new NumberConfig.Builder<Integer>("PlaceDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between placing crystals").build();
    Config<Boolean> sequentialPlace = new BooleanConfig.Builder("SequentialPlace")
            .setDescription("Places immediately after breaking a crystal")
            .setDefaultValue(false).build();
    Config<Boolean> protocolPlace = new BooleanConfig.Builder("Protocol")
            .setDescription("Prevents placements in 1x1 areas")
            .setDefaultValue(false).build();
    Config<Boolean> basePlace = new BooleanConfig.Builder("Support")
            .setDescription("Places an obsidian block if there is none")
            .setDefaultValue(false).build();
    Config<Void> placeConfig = new ConfigGroup.Builder("Place")
            .addAll(placeRange, placeDelay, sequentialPlace, protocolPlace, basePlace).build();

    Config<Boolean> targetItems = new BooleanConfig.Builder("TargetItems")
            .setDescription("Targets dropped items blocking placements")
            .setDefaultValue(false).build();
    Config<Boolean> forcePlace = new BooleanConfig.Builder("ForcePlace")
            .setDescription("Attempts to force crystal placements in blocked positions")
            .setDefaultValue(false).build();
    Config<Void> antiSurroundConfig = new ConfigGroup.Builder("AntiSurround")
            .addAll(targetItems, forcePlace).build();

    Config<Float> minDamage = new NumberConfig.Builder<Float>("MinDamage")
            .setMin(1.0f).setMax(10.0f).setDefaultValue(4.0f)
            .setDescription("The minimum damage to consider crystals").build();
    Config<Float> maxSelfDamage = new NumberConfig.Builder<Float>("MaxSelfDamage")
            .setMin(1.0f).setMax(20.0f).setDefaultValue(12.0f)
            .setDescription("The maximum damage a crystal can do to the player").build();
    Config<Boolean> overrideConfig = new BooleanConfig.Builder("Override")
            .setDescription("Allows overriding minimum damage (e.g. allows crystal spam)")
            .setDefaultValue(true).build();
    Config<Integer> minArmorDamage = new NumberConfig.Builder<Integer>("MinArmorDamage")
            .setMin(0).setMax(100).setDefaultValue(5).setFormat("%")
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("The minimum armor damage to consider spamming crystals").build();
    Config<Float> damageMultiplier = new NumberConfig.Builder<Float>("DamageMultiplier")
            .setMin(1.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("Place if we can kill target in this many crystals").build();
    Config<Boolean> ignoreTerrain = new BooleanConfig.Builder("IgnoreTerrain")
            .setDescription("Ignores explodable terrain during damage calculations")
            .setDefaultValue(false).build();
    Config<Void> damageConfig = new ConfigGroup.Builder("Damage")
            .addAll(minDamage, maxSelfDamage, overrideConfig, minArmorDamage,
                    damageMultiplier, ignoreTerrain).build();

    Config<RotateMode> rotateConfig = new EnumConfig.Builder<RotateMode>("Rotate")
            .setValues(RotateMode.values())
            .setDescription("Rotates to before interacting")
            .setDefaultValue(RotateMode.OFF).build();

    Config<Boolean> autoSwap = new BooleanConfig.Builder("AutoSwap")
            .setDescription("Automatically swaps to crystals before placing")
            .setDefaultValue(false).build();
    Config<Boolean> silentSwap = new BooleanConfig.Builder("SilentSwap")
            .setDescription("Silently swaps to crystals before placing")
            .setVisible(() -> autoSwap.getValue())
            .setDefaultValue(false).build();
    Config<Boolean> antiWeakness = new BooleanConfig.Builder("AntiWeakness")
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

    private CrystalData<EntityState> currentAttack;
    private CrystalData<BlockPos> currentPlace;

    private final Timer attackTimer = new NanoTimer();
    private final Timer placeTimer = new NanoTimer();

    private final Map<Integer, Long> attackPackets = new HashMap<>();
    private final Map<BlockPos, Long> placePackets = new HashMap<>();

    private final QueueAverage breakTime = new QueueAverage(20);
    private final PerSecond cps = new PerSecond();

    private final DecimalFormat numFormat = new DecimalFormat("0.0");

    private final ConcurrentMap<CrystalData<BlockPos>, Animation> fadeAnimations = new ConcurrentHashMap<>();

    public AutoCrystalModule()
    {
        super("AutoCrystal", new String[] {"CrystalAura"}, "Best CA on the market", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onDisable()
    {
        currentAttack = null;
        currentPlace = null;
        attackPackets.clear();
        placePackets.clear();
    }

    @Override
    public String getModuleData()
    {
        return String.format("%sms, %s", numFormat.format(breakTime.average()), cps.getPerSecond());
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener(priority = TickPriorities.AUTO_CRYSTAL)
    public void onTick(TickEvent.Pre event)
    {
        if (!shouldRunCalcs())
        {
            currentAttack = null;
            currentPlace = null;
            return;
        }

        List<CrystalData<BlockPos>> latestCrystalBases = Managers.CRYSTAL.getBaseResults();
        List<CrystalData<EntityState>> latestCrystalEntities = Managers.CRYSTAL.getEntityResults();

        currentAttack = getBestData(latestCrystalEntities);

        List<CrystalData<BlockPos>> placements = getPlacements(latestCrystalBases);
        currentPlace = getBestData(basePlace.getValue() && placements.isEmpty() ? latestCrystalBases : placements);
    }

    @EventListener(priority = TickPriorities.AUTO_CRYSTAL)
    public void onClientRotation(ClientRotationEvent event)
    {
        if (checkNull() || event.isCanceled())
        {
            return;
        }

        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
            return;
        }

        Hand hand = getCrystalHand();

        boolean silentRotated = false;
        float[] rotations = null;

        Vec3d crystalVec;
        if (currentAttack != null)
        {
            EntityState crystalState = currentAttack.getCrystalData();
            crystalVec = crystalState.getPos();
            rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalVec);
            if (rotateConfig.getValue() == RotateMode.SILENT)
            {
                Managers.ROTATION.setSilentRotation(new Rotation(rotations[0], rotations[1]));
                silentRotated = true;
            }

            if (attackTimer.hasPassed(breakDelay.getValue()))
            {
                attackCrystal(crystalState.getId(), hand);
                attackTimer.reset();
            }
        }

        if (currentPlace != null)
        {
            BlockPos crystalPos = currentPlace.getCrystalData();
            if (basePlace.getValue() && !hasCrystalBaseBlock(crystalPos))
            {
                if (!runSingleObbyPlacement(crystalPos))
                {
                    currentPlace = null;
                    return;
                }
            }

            crystalVec = crystalPos.toBottomCenterPos().add(0.0, 1.0, 0.0);
            rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalVec);
            if (rotateConfig.getValue() == RotateMode.SILENT && !silentRotated)
            {
                Managers.ROTATION.setSilentRotation(new Rotation(rotations[0], rotations[1]));
                silentRotated = true;
            }

            if (placeTimer.hasPassed(placeDelay.getValue()))
            {
                placeCrystal(crystalPos, hand);
                placeTimer.reset();
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

        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
            return;
        }

        Vec3d crystalPos = event.getPos();
        BlockPos crystalBase = BlockPos.ofFloored(crystalPos.offset(Direction.DOWN, 1.0));

        if (placePackets.remove(crystalBase) == null)
        {
            return;
        }

        cps.count();

        if (sequentialBreak.getValue())
        {
            Hand hand = getCrystalHand();

            attackCrystal(event.getEntityId(), hand);
            attackTimer.reset();

            if (currentPlace != null && sequentialPlace.getValue())
            {
                placeCrystal(currentPlace.getCrystalData(), hand);
            }
        }
    }

    @EventListener
    public void onExplosion(ExplosionEvent event)
    {
        if (checkNull())
        {
            return;
        }

        for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
        {
            if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(event.getCenter()) <= 144.0)
            {
                mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                Long time = attackPackets.remove(id);
                if (time != null)
                {
                    breakTime.add(System.currentTimeMillis() - time);
                }
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (currentPlace != null)
        {
            fadeAnimations.put(currentPlace, new Animation(true, 250));
        }

        for (Map.Entry<CrystalData<BlockPos>, Animation> entry : fadeAnimations.entrySet())
        {
            CrystalData<BlockPos> placeData = entry.getKey();
            BlockPos placePos = placeData.getCrystalData();
            Animation anim = entry.getValue();

            if (anim.getFactor() <= 0.01)
            {
                fadeAnimations.remove(placeData);
                continue;
            }

            float animFactor = (float) Easing.SMOOTH_STEP.ease(anim.getFactor());
            anim.setState(false);
            BoxRender.FILL.render(event.getMatrixStack(),
                    placePos, ThemeModule.INSTANCE.getPrimaryColor().getRGB(), animFactor);

            double damage = ExplosionUtil.getAppliedDamageToEntity(placeData.getTarget().getEntity(), (float) placeData.getDamageToTarget());
            Managers.RENDER.renderNametag(event.getMatrixStack(),
                    placePos.toCenterPos(),
                    0.003f,
                    numFormat.format(damage),
                    ColorUtil.withTransparency(-1, animFactor));
        }
    }

    private void attackCrystal(int crystalId, Hand hand)
    {
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);

        boolean canBreakCrystal = weakness == null || (strength != null && strength.getAmplifier() >= weakness.getAmplifier());
        if (!canBreakCrystal)
        {
            int slot = getAntiWeaknessSlot();
            if (slot == -1 || !Managers.INVENTORY.startSwap(slot, silentType.getValue()))
            {
                return;
            }
        }

        sendAttackPacketsInternal(crystalId, swingConfig.getValue(), hand);

        if (!canBreakCrystal)
        {
            Managers.INVENTORY.endSwap(silentType.getValue());
        }

        attackPackets.put(crystalId, System.currentTimeMillis());
    }

    private void placeCrystal(BlockPos blockPos, Hand hand)
    {
        Box box = new Box(blockPos);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d cut = new Vec3d(MathHelper.clamp(eyePos.getX(), box.minX, box.maxX),
                MathHelper.clamp(eyePos.getY(), box.minY, box.maxY),
                MathHelper.clamp(eyePos.getZ(), box.minZ, box.maxZ));

        Direction placeDir = getPlaceDirection(blockPos, box, eyePos, cut);
        BlockHitResult result = new BlockHitResult(cut, placeDir, blockPos, box.contains(eyePos));
        if (autoSwap.getValue() && silentSwap.getValue())
        {
            int slot = InventoryUtil.getItemSlot(Items.END_CRYSTAL, silentType.getValue());
            if (slot == -1 || !Managers.INVENTORY.startSwap(slot, silentType.getValue()))
            {
                return;
            }
        }

        if (!Managers.INVENTORY.isHolding(Items.END_CRYSTAL, hand))
        {
            return;
        }

        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (swingConfig.getValue())
        {
            mc.player.swingHand(hand);
        } else
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }

        if (autoSwap.getValue() && silentSwap.getValue())
        {
            Managers.INVENTORY.endSwap(silentType.getValue());
        }

        placePackets.put(blockPos, System.currentTimeMillis());
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

    public boolean shouldRunCalcs()
    {
        return isEnabled() && !mc.player.isSpectator();
    }

    private List<CrystalData<BlockPos>> getPlacements(List<CrystalData<BlockPos>> crystalData)
    {
        return crystalData.stream().filter(d -> hasCrystalBaseBlock(d.getCrystalData())).toList();
    }

    private <T> CrystalData<T> getBestData(List<CrystalData<T>> crystals)
    {
        if (crystals.isEmpty())
        {
            return null;
        }

        CrystalData<T> bestCrystal = null;
        float bestDamage = 0.0f;

        for (CrystalData<T> data : crystals)
        {
            LivingEntity entity = (LivingEntity) data.getTarget().getEntity();
            float baseDamage = (float) data.getDamageToTarget();

            if (entity.isDead() || baseDamage < bestDamage)
            {
                continue;
            }

            float damage = ExplosionUtil.getAppliedDamageToEntity(entity, baseDamage);
            if (damage > bestDamage)
            {
                bestDamage = damage;
                bestCrystal = data;
            }
        }

        if (bestDamage < minDamage.getValue())
        {
            return null;
        }

        return bestCrystal;
    }

    private boolean hasCrystalBaseBlock(BlockPos pos)
    {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return false;
        }

        return hasSpaceToPlaceCrystal(mc.world, pos);
    }

    public boolean hasSpaceToPlaceCrystal(BlockView blockView, BlockPos blockPos)
    {
        BlockPos p2 = blockPos.up();
        BlockState state2 = blockView.getBlockState(p2);
        if (protocolPlace.getValue() && !blockView.getBlockState(p2.up()).isAir())
        {
            return false;
        }

        return state2.isAir() || state2.isOf(Blocks.FIRE);
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
                || itemStack.getItem().getTranslationKey().contains("axe"));
    }

    public boolean canTargetEntity(Entity entity)
    {
        return entity instanceof PlayerEntity player
                && targetPlayers.getValue()
                && (targetNakeds.getValue() || player.getArmor() > 0)
                || EntityUtil.isHostile(entity) && targetHostiles.getValue()
                || EntityUtil.isPassive(entity) && targetPassives.getValue();
    }
}

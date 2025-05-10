package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.RunTickEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.impl.module.CombatModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.exploit.FastLatencyModule;
import net.shoreline.client.impl.module.world.AutoMineModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.collection.EvictingQueue;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.math.PerSecondCounter;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.client.util.world.ExplosionUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.loader.Loader;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author linus
 * @since 1.0
 */
public class AutoCrystalModule extends CombatModule
{
    private static AutoCrystalModule INSTANCE;

    Config<Boolean> whileMiningConfig = register(new BooleanConfig("WhileMining", "Allows attacking while mining blocks", false));
    Config<Float> targetRangeConfig = register(new NumberConfig<>("EnemyRange", "Range to search for potential enemies", 1.0f, 10.0f, 13.0f));
    Config<Boolean> instantConfig = register(new BooleanConfig("Instant", "Instantly attacks crystals when they spawn", false));
    Config<Sequential> sequentialConfig = register(new EnumConfig<>("Sequential", "Places a crystal after spawn", Sequential.NONE, Sequential.values()));
    Config<Boolean> idPredictConfig = register(new BooleanConfig("BreakPredict", "Attempts to predict crystal entity ids", false));
    Config<Boolean> instantCalcConfig = register(new BooleanConfig("Instant-Calc", "Calculates a crystal when it spawns and attacks if it meets MINIMUM requirements, this will result in non-ideal crystal attacks", false, () -> false));
    Config<Float> instantDamageConfig = register(new NumberConfig<>("InstantDamage", "Minimum damage to attack crystals instantly", 1.0f, 6.0f, 10.0f, () -> false));
    Config<Boolean> instantMaxConfig = register(new BooleanConfig("InstantMax", "Attacks crystals instantly if they exceed the previous max attack damage (Note: This is still not a perfect check because the next tick could have better damages)", true, () -> false));
    Config<Boolean> raytraceConfig = register(new BooleanConfig("Raytrace", "Raytrace to crystal position", true));
    Config<Boolean> swingConfig = register(new BooleanConfig("Swing", "Swing hand when placing and attacking crystals", true));
    // ROTATE SETTINGS
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotate before placing and breaking", false));
    // Config<Boolean> rotateSilentConfig = register(new BooleanConfig("RotateSilent", "Silently updates rotations to server", false));
    Config<Rotate> strictRotateConfig = register(new EnumConfig<>("YawStep", "Rotates yaw over multiple ticks to prevent certain rotation flags in NCP", Rotate.OFF, Rotate.values(), () -> rotateConfig.getValue()));
    Config<Integer> rotateLimitConfig = register(new NumberConfig<>("YawStep-Limit", "Maximum yaw rotation in degrees for one tick", 1, 180, 180, NumberDisplay.DEGREES, () -> rotateConfig.getValue() && strictRotateConfig.getValue() != Rotate.OFF));
    // Config<Boolean> rotateTickFactorConfig = register(new BooleanConfig("Rotate-TickReduction", "Factors in angles when calculating crystals", false, () -> rotateConfig.getValue() && strictRotateConfig.getValue() != Rotate.OFF));

    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Target players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Target monsters", false));
    Config<Boolean> neutralsConfig = register(new BooleanConfig("Neutrals", "Target neutrals", false));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Target animals", false));
    Config<Boolean> shulkersConfig = register(new BooleanConfig("Shulkers", "Target shulker boxes", false));
    // BREAK SETTINGS
    Config<Float> breakSpeedConfig = register(new NumberConfig<>("BreakSpeed", "Speed to break crystals", 0.1f, 18.0f, 20.0f));
    Config<Float> attackDelayConfig = register(new NumberConfig<>("AttackDelay", "Added delays", 0.0f, 0.0f, 5.0f));
    Config<Integer> attackFactorConfig = register(new NumberConfig<>("AttackFactor", "Factor of attack delay", 0, 0, 3, () -> attackDelayConfig.getValue() > 0.0));
    Config<Float> attackLimitConfig = register(new NumberConfig<>("AttackLimit", "The number of attacks before considering a crystal unbreakable", 0.5f, 1.5f, 20.0f));
    // Config<Float> randomSpeedConfig = register(new NumberConfig<>("RandomSpeed", "Randomized delay for breaking crystals", 0.0f, 0.0f, 10.0f));
    Config<Boolean> breakDelayConfig = register(new BooleanConfig("BreakDelay", "Uses attack latency to calculate break delays", false));
    Config<Float> breakTimeoutConfig = register(new NumberConfig<>("BreakTimeout", "Time after waiting for the average break time before considering a crystal attack failed", 0.0f, 3.0f, 10.0f, () -> breakDelayConfig.getValue()));
    Config<Float> minTimeoutConfig = register(new NumberConfig<>("MinTimeout", "Minimum time before considering a crystal break/place failed", 0.0f, 5.0f, 20.0f, () -> breakDelayConfig.getValue()));
    Config<Integer> ticksExistedConfig = register(new NumberConfig<>("TicksExisted", "Minimum ticks alive to consider crystals for attack", 0, 0, 10));
    Config<Float> breakRangeConfig = register(new NumberConfig<>("BreakRange", "Range to break crystals", 0.1f, 4.0f, 6.0f));
    Config<Float> maxYOffsetConfig = register(new NumberConfig<>("MaxYOffset", "Maximum crystal y-offset difference", 1.0f, 5.0f, 10.0f));
    Config<Float> breakWallRangeConfig = register(new NumberConfig<>("BreakWallRange", "Range to break crystals through walls", 0.1f, 4.0f, 6.0f));
    Config<Swap> antiWeaknessConfig = register(new EnumConfig<>("AntiWeakness", "Swap to tools before attacking crystals", Swap.OFF, Swap.values()));
    Config<Float> swapDelayConfig = register(new NumberConfig<>("SwapPenalty", "Delay for attacking after swapping items which prevents NCP flags", 0.0f, 0.0f, 10.0f));
    //
    Config<Boolean> inhibitConfig = register(new BooleanConfig("Inhibit", "Prevents excessive attacks", true));
    Config<Boolean> placeConfig = register(new BooleanConfig("Place", "Places crystals to damage enemies. Place settings will only function if this setting is enabled.", true));
    Config<Float> placeSpeedConfig = register(new NumberConfig<>("PlaceSpeed", "Speed to place crystals", 0.1f, 18.0f, 20.0f, () -> placeConfig.getValue()));
    Config<Float> placeRangeConfig = register(new NumberConfig<>("PlaceRange", "Range to place crystals", 0.1f, 4.0f, 6.0f, () -> placeConfig.getValue()));
    Config<Float> placeWallRangeConfig = register(new NumberConfig<>("PlaceWallRange", "Range to place crystals through walls", 0.1f, 4.0f, 6.0f, () -> placeConfig.getValue()));
    Config<Boolean> placeRangeEyeConfig = register(new BooleanConfig("PlaceRangeEye", "Calculates place ranges starting from the eye position of the player", false, () -> placeConfig.getValue()));
    Config<Boolean> placeRangeCenterConfig = register(new BooleanConfig("PlaceRangeCenter", "Calculates place ranges to the center of the block", true, () -> placeConfig.getValue()));
    Config<Swap> autoSwapConfig = register(new EnumConfig<>("Swap", "Swaps to an end crystal before placing if the player is not holding one", Swap.OFF, Swap.values(), () -> placeConfig.getValue()));
    // Config<Float> alternateSpeedConfig = register(new NumberConfig<>("AlternateSpeed", "Speed for alternative swapping crystals", 1.0f, 18.0f, 20.0f, () -> placeConfig.getValue() && autoSwapConfig.getValue() == Swap.SILENT_ALT));
    Config<Boolean> antiSurroundConfig = register(new BooleanConfig("AntiSurround", "Places on mining blocks that when broken, can be placed on to damage enemies. Instantly destroys items spawned from breaking block and allows faster placing", false, () -> placeConfig.getValue()));
    Config<ForcePlace> forcePlaceConfig = register(new EnumConfig<>("PreventReplace", "Attempts to replace crystals in surrounds", ForcePlace.NONE, ForcePlace.values()));
    Config<Boolean> breakValidConfig = register(new BooleanConfig("Strict", "Only places crystals that can be attacked", false, () -> placeConfig.getValue()));
    Config<Boolean> strictDirectionConfig = register(new BooleanConfig("StrictDirection", "Interacts with only visible directions when placing crystals", false, () -> placeConfig.getValue()));
    Config<Placements> placementsConfig = register(new EnumConfig<>("Placements", "Version standard for placing end crystals", Placements.NATIVE, Placements.values(), () -> placeConfig.getValue()));
    // Damage settings
    Config<Float> minDamageConfig = register(new NumberConfig<>("MinDamage", "Minimum damage required to consider attacking or placing an end crystal", 1.0f, 4.0f, 10.0f));
    Config<Float> maxLocalDamageConfig = register(new NumberConfig<>("MaxLocalDamage", "The maximum player damage", 4.0f, 12.0f, 20.0f));
    Config<Boolean> assumeArmorConfig = register(new BooleanConfig("AssumeBestArmor", "Assumes Prot 0 armor is max armor", false));
    Config<Boolean> armorBreakerConfig = register(new BooleanConfig("ArmorBreaker", "Attempts to break enemy armor with crystals", true));
    Config<Float> armorScaleConfig = register(new NumberConfig<>("ArmorScale", "Armor damage scale before attempting to break enemy armor with crystals", 1.0f, 5.0f, 20.0f, NumberDisplay.PERCENT, () -> armorBreakerConfig.getValue()));
    Config<Float> lethalMultiplier = register(new NumberConfig<>("LethalMultiplier", "If we can kill an enemy with this many crystals, disregard damage values", 0.0f, 1.5f, 4.0f));
    Config<Boolean> antiTotemConfig = register(new BooleanConfig("Lethal-Totem", "Predicts totems and places crystals to instantly double pop and kill the target", false, () -> placeConfig.getValue()));
    Config<Boolean> lethalDamageConfig = register(new BooleanConfig("Lethal-DamageTick", "Places lethal crystals only on ticks where they damage entities", false));
    Config<Boolean> safetyConfig = register(new BooleanConfig("Safety", "Accounts for total player safety when attacking and placing crystals", true));
    Config<Boolean> safetyOverride = register(new BooleanConfig("SafetyOverride", "Overrides the safety checks if the crystal will kill an enemy", false));
    Config<Boolean> blockDestructionConfig = register(new BooleanConfig("BlockDestruction", "Accounts for explosion block destruction when calculating damages", false));
    Config<Boolean> selfExtrapolateConfig = register(new BooleanConfig("SelfExtrapolate", "Accounts for motion when calculating self damage", false));
    Config<Integer> extrapolateTicksConfig = register(new NumberConfig<>("ExtrapolationTicks", "Accounts for motion when calculating enemy positions, not fully accurate.", 0, 0, 10));
    // Render settings
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders the current placement", true));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 250, 1000, () -> false));
    Config<Boolean> disableDeathConfig = register(new BooleanConfig("DisableOnDeath", "Disables during disconnect/death", false));
    Config<Boolean> debugConfig = new BooleanConfig("Debug", "Adds extra debug info to arraylist", false);
    Config<Boolean> debugDamageConfig = new BooleanConfig("Debug-Damage", "Renders damage", false, () -> renderConfig.getValue());
    //
    private DamageData<EndCrystalEntity> attackCrystal;
    private DamageData<BlockPos> placeCrystal;
    //
    private BlockPos renderPos;
    private double renderDamage;
    private BlockPos renderSpawnPos;
    //
    private Vec3d crystalRotation;
    private boolean attackRotate;
    private boolean rotated;
    private float[] silentRotations;
    private float calculatePlaceCrystalTime = 0;
    //
    private static final Box FULL_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);
    private static final Box HALF_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private final CacheTimer lastAttackTimer = new CacheTimer();
    private final Timer lastPlaceTimer = new CacheTimer();
    private final Timer lastSwapTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    // default NCP config
    // fight.speed: limit: 13
    // shortterm: ticks: 8
    // limitforseconds: half: 8, one: 15, two: 30, four: 60, eight: 100
    private final Deque<Long> attackLatency = new EvictingQueue<>(20);
    private final Map<Integer, Long> attackPackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> placePackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final PerSecondCounter crystalCounter = new PerSecondCounter();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private long predictId;
    // Antistuck
    private final Map<Integer, Integer> antiStuckCrystals = new HashMap<>();
    private final List<AntiStuckData> stuckCrystals = new CopyOnWriteArrayList<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AutoCrystalModule()
    {
        super("AutoCrystal", "Attacks entities with end crystals",
                ModuleCategory.COMBAT, 750);
        INSTANCE = this;

        if (!Loader.SESSION.getUserType().equals("release"))
        {
            register(debugConfig);
            register(debugDamageConfig);
        }
    }

    public static AutoCrystalModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        if (debugConfig.getValue())
        {
            return String.format("%sms, %.0f, %dms, %d".formatted(
                    new DecimalFormat("0.00")
                            .format(calculatePlaceCrystalTime / 1E6),
                    placeCrystal == null ? 0 : lastAttackTimer.getLastResetTime() / 1E6,
                    lastAttackTimer.passed(((20.0f - breakSpeedConfig.getValue()) * 50.0f) + 2000.0f) ? 0 : getBreakMs(),
                    crystalCounter.getPerSecond()));
        }
        else
        {
            return String.format("%dms, %d",
                    lastAttackTimer.passed(((20.0f - breakSpeedConfig.getValue()) * 50.0f) + 2000.0f) ? 0 : getBreakMs(),
                    crystalCounter.getPerSecond());
        }
    }

    @Override
    public void onDisable()
    {
        renderPos = null;
        attackCrystal = null;
        placeCrystal = null;
        crystalRotation = null;
        silentRotations = null;
        calculatePlaceCrystalTime = 0;
        stuckCrystals.clear();
        attackPackets.clear();
        antiStuckCrystals.clear();
        placePackets.clear();
        attackLatency.clear();
        fadeList.clear();
        setStage("NONE");
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        if (disableDeathConfig.getValue())
        {
            disable();
        }
        else
        {
            onDisable();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        if (mc.player.isSpectator() || isSilentSwap(autoSwapConfig.getValue()) && AutoMineModule.getInstance().isSilentSwapping())
        {
            return;
        }

        for (AntiStuckData d : stuckCrystals)
        {
            double dist = mc.player.squaredDistanceTo(d.pos());
            double diff = d.stuckDist() - dist;
            if (diff > 0.5)
            {
                stuckCrystals.remove(d);
            }
        }

        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
                || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed())
        {
            autoSwapTimer.reset();
        }
        renderPos = null;
        ArrayList<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        List<BlockPos> blocks = getSphere(placeRangeEyeConfig.getValue() ? mc.player.getEyePos() : mc.player.getPos());
        long timePre = System.nanoTime();
        if (placeConfig.getValue())
        {
            placeCrystal = calculatePlaceCrystal(blocks, entities);
        }
        attackCrystal = calculateAttackCrystal(entities);
        if (attackCrystal == null)
        {
            if (placeCrystal != null)
            {
                EndCrystalEntity crystalEntity = intersectingCrystalCheck(placeCrystal.getDamageData());
                if (crystalEntity != null)
                {
                    double self = ExplosionUtil.getDamageTo(mc.player, crystalEntity.getPos(),
                            blockDestructionConfig.getValue(), selfExtrapolateConfig.getValue() ? extrapolateTicksConfig.getValue() : 0, false);
                    if (!safetyConfig.getValue() || !playerDamageCheck(self))
                    {
                        attackCrystal = new DamageData<>(crystalEntity, placeCrystal.getAttackTarget(),
                                placeCrystal.getDamage(), self, crystalEntity.getBlockPos().down(), false);
                    }
                }
            }
            calculatePlaceCrystalTime = System.nanoTime() - timePre;
        }

        if (inhibitConfig.getValue() && attackCrystal != null
                && attackPackets.containsKey(attackCrystal.getDamageData().getId()))
        {
            float delay;
            if (attackDelayConfig.getValue() > 0.0)
            {
                float attackFactor = 50.0f / Math.max(1.0f, attackFactorConfig.getValue());
                delay = attackDelayConfig.getValue() * attackFactor;
            }
            else
            {
                delay = 1000.0f - breakSpeedConfig.getValue() * 50.0f;
            }
            lastAttackTimer.setDelay(delay + 100.0f);
            attackPackets.remove(attackCrystal.getDamageData().getId());
        }

        float breakDelay = getBreakDelay();
        if (breakDelayConfig.getValue())
        {
            breakDelay = Math.max(minTimeoutConfig.getValue() * 50.0f, getBreakMs() + breakTimeoutConfig.getValue() * 50.0f);
        }
        attackRotate = attackCrystal != null && attackDelayConfig.getValue() <= 0.0 && lastAttackTimer.passed(breakDelay);
        if (attackCrystal != null)
        {
            crystalRotation = attackCrystal.damageData.getPos();
        }
        else if (placeCrystal != null)
        {
            crystalRotation = placeCrystal.damageData.toCenterPos().add(0.0, 0.5, 0.0);
        }
        if (rotateConfig.getValue() && crystalRotation != null && (placeCrystal == null || canHoldCrystal()))
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalRotation);
            if (strictRotateConfig.getValue() == Rotate.FULL || strictRotateConfig.getValue() == Rotate.SEMI && attackRotate)
            {
                float yaw;
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotations[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f)
                {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimitConfig.getValue();
                if (diff1 > rotateLimitConfig.getValue())
                {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                }
                else
                {
                    yaw = rotations[0];
                    rotated = true;
                    crystalRotation = null;
                }
                rotations[0] = yaw;
            }
            else
            {
                rotated = true;
                crystalRotation = null;
            }
            setRotation(rotations[0], rotations[1]);
        }
        else
        {
            silentRotations = null;
        }
        if (isRotationBlocked() || !rotated && rotateConfig.getValue())
        {
            return;
        }
//        if (rotateSilentConfig.getValue() && silentRotations != null) {
//            setRotationSilent(silentRotations[0], silentRotations[1]);
//        }
        final Hand hand = getCrystalHand();
        if (attackCrystal != null)
        {
            // ChatUtil.clientSendMessage("yaw: " + rotations[0] + ", pitch: " + rotations[1]);
            if (attackRotate)
            {
                // ChatUtil.clientSendMessage("break range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(attackCrystal.getDamageData().getPos())));
                attackCrystal(attackCrystal.getDamageData(), hand);
                setStage("ATTACKING");
                lastAttackTimer.reset();
            }
        }
        boolean placeRotate = lastPlaceTimer.passed(1000.0f - placeSpeedConfig.getValue() * 50.0f);
        if (placeCrystal != null)
        {
            renderPos = placeCrystal.getDamageData();
            renderDamage = placeCrystal.getDamage();
            if (placeRotate)
            {
                // ChatUtil.clientSendMessage("place range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(placeCrystal.getDamageData().toCenterPos())));
                placeCrystal(placeCrystal.getDamageData(), hand);
                setStage("PLACING");
                lastPlaceTimer.reset();
            }
        }
    }

    @EventListener
    public void onRunTick(RunTickEvent event)
    {
        if (mc.player == null)
        {
            return;
        }
        final Hand hand = getCrystalHand();
        if (attackDelayConfig.getValue() > 0.0)
        {
            float attackFactor = 50.0f / Math.max(1.0f, attackFactorConfig.getValue());
            if (attackCrystal != null && lastAttackTimer.passed(attackDelayConfig.getValue() * attackFactor))
            {
                attackCrystal(attackCrystal.getDamageData(), hand);
                lastAttackTimer.reset();
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            BlockPos renderPos1 = null;
            double factor = 0.0f;
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                if (set.getKey() == renderPos)
                {
                    continue;
                }

                if (set.getValue().getFactor() > factor)
                {
                    renderPos1 = set.getKey();
                    factor = set.getValue().getFactor();
                }

                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
                Color boxColor = ColorsModule.getInstance().getColor(boxAlpha);
                Color lineColor = ColorsModule.getInstance().getColor(lineAlpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }

            if (debugDamageConfig.getValue() && renderPos1 != null)
            {
                RenderManager.renderSign(String.format("%.1f", renderDamage),
                        renderPos1.toCenterPos(), new Color(255, 255, 255, (int) (255.0f * factor)).getRGB());
            }

            RenderBuffers.postRender();

            fadeList.entrySet().removeIf(e ->
                    e.getValue().getFactor() == 0.0);

            if (renderPos != null && isHoldingCrystal())
            {
                Animation animation = new Animation(true, fadeTimeConfig.getValue());
                fadeList.put(renderPos, animation);
            }
        }
    }

    @EventListener(priority = Integer.MAX_VALUE)
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handleServerPackets(packet1);
            }
        }
        else
        {
            handleServerPackets(event.getPacket());
        }
    }

    private void handleServerPackets(Packet<?> serverPacket)
    {
        if (serverPacket instanceof ExplosionS2CPacket packet)
        {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
            {
                if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 144.0)
                {
                    mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
                    antiStuckCrystals.remove(entity.getId());
                    Long attackTime = attackPackets.remove(entity.getId());
                    if (attackTime != null)
                    {
                        attackLatency.add(System.currentTimeMillis() - attackTime);
                    }
                }
            }
        }

        if (serverPacket instanceof PlaySoundS2CPacket packet)
        {
            if (packet.getSound().value() == SoundEvents.ENTITY_GENERIC_EXPLODE.value() && packet.getCategory() == SoundCategory.BLOCKS)
            {
                for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
                {
                    if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 144.0)
                    {
                        mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
                        antiStuckCrystals.remove(entity.getId());
                        Long attackTime = attackPackets.remove(entity.getId());
                        if (attackTime != null)
                        {
                            attackLatency.add(System.currentTimeMillis() - attackTime);
                        }
                    }
                }
            }
        }

        if (serverPacket instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                antiStuckCrystals.remove(id);
                Long attackTime = attackPackets.remove(id);
                if (attackTime != null)
                {
                    attackLatency.add(System.currentTimeMillis() - attackTime);
                }
            }
        }

        if (serverPacket instanceof ExperienceOrbSpawnS2CPacket packet && packet.getEntityId() > predictId)
        {
            predictId = packet.getEntityId();
        }

        if (serverPacket instanceof EntitySpawnS2CPacket packet && packet.getEntityId() > predictId)
        {
            predictId = packet.getEntityId();
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event)
    {
        if (!(event.getEntity() instanceof EndCrystalEntity crystalEntity))
        {
            return;
        }
        Vec3d crystalPos = crystalEntity.getPos();
        BlockPos blockPos = BlockPos.ofFloored(crystalPos.add(0.0, -1.0, 0.0));
        renderSpawnPos = blockPos;
        Long time = placePackets.remove(blockPos);
        attackRotate = time != null;
        if (attackRotate)
        {
            crystalCounter.updateCounter();
        }
        if (!instantConfig.getValue())
        {
            return;
        }
        if (attackRotate)
        {
            final Hand hand = getCrystalHand();
            attackInternal(crystalEntity, hand);
            setStage("ATTACKING");
            lastAttackTimer.reset();
            if (sequentialConfig.getValue() == Sequential.NORMAL)
            {
                placeSequentialCrystal(hand);
            }
        }
        else if (instantCalcConfig.getValue())
        {
            if (attackRangeCheck(crystalPos))
            {
                return;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalPos,
                    blockDestructionConfig.getValue(), selfExtrapolateConfig.getValue() ? extrapolateTicksConfig.getValue() : 0, false);
            if (playerDamageCheck(selfDamage))
            {
                return;
            }
            for (Entity entity : mc.world.getEntities())
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || Managers.SOCIAL.isFriend(entity.getName()))
                {
                    continue;
                }
                double crystalDist = crystalPos.squaredDistanceTo(entity.getPos());
                if (crystalDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue())
                {
                    continue;
                }

                double damage = ExplosionUtil.getDamageTo(entity, crystalPos, blockDestructionConfig.getValue(),
                        extrapolateTicksConfig.getValue(), assumeArmorConfig.getValue());
                // TODO: Test this
                DamageData<EndCrystalEntity> data = new DamageData<>(crystalEntity,
                        entity, damage, selfDamage, crystalEntity.getBlockPos().down(), false);
                attackRotate = damage > instantDamageConfig.getValue() || attackCrystal != null
                        && damage >= attackCrystal.getDamage() && instantMaxConfig.getValue()
                        || entity instanceof LivingEntity entity1 && isCrystalLethalTo(data, entity1);
                if (attackRotate)
                {
                    final Hand hand = getCrystalHand();
                    attackInternal(crystalEntity, hand);
                    setStage("ATTACKING");
                    lastAttackTimer.reset();
                    if (sequentialConfig.getValue() == Sequential.NORMAL)
                    {
                        placeSequentialCrystal(hand);
                    }
                    break;
                }
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket)
        {
            lastSwapTimer.reset();
        }
    }

    public boolean isAttacking()
    {
        return attackCrystal != null;
    }

    public boolean isPlacing()
    {
        return placeCrystal != null && isHoldingCrystal();
    }

    public void attackCrystal(EndCrystalEntity entity, Hand hand)
    {
        if (attackCheckPre(hand))
        {
            return;
        }
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
        if (weakness != null && (strength == null || weakness.getAmplifier() > strength.getAmplifier()))
        {
            int slot = -1;
            for (int i = 0; i < 9; ++i)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem
                        || stack.getItem() instanceof AxeItem
                        || stack.getItem() instanceof PickaxeItem))
                {
                    slot = i;
                    break;
                }
            }
            if (slot != -1)
            {
                boolean canSwap = slot != Managers.INVENTORY.getServerSlot() && (antiWeaknessConfig.getValue() != Swap.NORMAL || autoSwapTimer.passed(500));
                if (antiWeaknessConfig.getValue() != Swap.OFF && canSwap)
                {
                    if (antiWeaknessConfig.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (antiWeaknessConfig.getValue() == Swap.SILENT)
                    {
                        Managers.INVENTORY.setSlot(slot);
                    }
                    else
                    {
                        Managers.INVENTORY.setClientSlot(slot);
                    }
                }
                attackInternal(entity, Hand.MAIN_HAND);
                if (canSwap)
                {
                    if (antiWeaknessConfig.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (antiWeaknessConfig.getValue() == Swap.SILENT)
                    {
                        Managers.INVENTORY.syncToClient();
                    }
                }

                if (sequentialConfig.getValue() == Sequential.STRICT)
                {
                    placeSequentialCrystal(hand);
                }
            }
        }
        else
        {
            attackInternal(entity, hand);
            if (sequentialConfig.getValue() == Sequential.STRICT)
            {
                placeSequentialCrystal(hand);
            }
        }
    }

    private void attackInternal(EndCrystalEntity crystalEntity, Hand hand)
    {
        attackInternal(crystalEntity.getId(), hand);
    }

    private void attackInternal(int crystalEntity, Hand hand)
    {
        hand = hand != null ? hand : Hand.MAIN_HAND;
        EndCrystalEntity entity2 = new EndCrystalEntity(mc.world, 0.0, 0.0, 0.0);
        entity2.setId(crystalEntity);
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity2, mc.player.isSneaking());
        Managers.NETWORK.sendPacket(packet);
        if (swingConfig.getValue())
        {
            mc.player.swingHand(hand);
        }
        else
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }

        attackPackets.put(crystalEntity, System.currentTimeMillis());
        Integer antiStuckCount = antiStuckCrystals.get(crystalEntity);
        if (antiStuckCount != null)
        {
            antiStuckCrystals.replace(crystalEntity, antiStuckCount + 1);
        }
        else
        {
            antiStuckCrystals.put(crystalEntity, 1);
        }
    }

    private void placeSequentialCrystal(Hand hand)
    {
        if (placeCrystal == null)
        {
            return;
        }
        int latency = FastLatencyModule.getInstance().isEnabled() ? (int)
                FastLatencyModule.getInstance().getLatency() : Managers.NETWORK.getClientLatency();
        if (!Managers.NETWORK.is2b2t() || latency >= 50)
        {
            placeCrystal(placeCrystal.getBlockPos(), hand);
        }
    }

    private void placeCrystal(BlockPos blockPos, Hand hand)
    {
        if (isRotationBlocked() || !rotated && rotateConfig.getValue())
        {
            return;
        }

        placeCrystal(blockPos, hand, true);
    }

    public void placeCrystal(BlockPos blockPos, Hand hand, boolean checkPlacement)
    {
        if (checkPlacement && checkCanUseCrystal())
        {
            return;
        }
        Direction sidePlace = getPlaceDirection(blockPos);
        // Vec3d vec3d = mc.player.getCameraPosVec(1.0f);
        // Vec3d vec3d1 = RotationUtil.getRotationVector();
        // Vec3d vec3d3 = vec3d.add(vec3d1.x * placeRangeConfig.getValue(),
        //        vec3d1.y * placeRangeConfig.getValue(), vec3d1.z * placeRangeConfig.getValue());
        // HitResult hitResult = mc.world.raycast(new RaycastContext(vec3d, vec3d3,
        //        RaycastContext.ShapeType.OUTLINE,
        //        RaycastContext.FluidHandling.NONE, mc.player));
        BlockHitResult result = new BlockHitResult(blockPos.toCenterPos(), sidePlace, blockPos, false);
        if (autoSwapConfig.getValue() != Swap.OFF && hand != Hand.OFF_HAND && getCrystalHand() == null)
        {
            if (isSilentSwap(autoSwapConfig.getValue()) && InventoryUtil.count(Items.END_CRYSTAL) == 0)
            {
                return;
            }
            int crystalSlot = getCrystalSlot();
            if (crystalSlot != -1)
            {
                boolean canSwap = crystalSlot != Managers.INVENTORY.getServerSlot() && (autoSwapConfig.getValue() != Swap.NORMAL || autoSwapTimer.passed(500));
                if (canSwap)
                {
                    if (autoSwapConfig.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (autoSwapConfig.getValue() == Swap.SILENT)
                    {
                        Managers.INVENTORY.setSlot(crystalSlot);
                    }
                    else
                    {
                        Managers.INVENTORY.setClientSlot(crystalSlot);
                    }
                }
                placeInternal(result, Hand.MAIN_HAND);
                placePackets.put(blockPos, System.currentTimeMillis());
                if (canSwap)
                {
                    if (autoSwapConfig.getValue() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (autoSwapConfig.getValue() == Swap.SILENT)
                    {
                        Managers.INVENTORY.syncToClient();
                    }
                }
            }
        }
        else if (isHoldingCrystal())
        {
            placeInternal(result, hand);
            placePackets.put(blockPos, System.currentTimeMillis());
        }
    }

    private void placeInternal(BlockHitResult result, Hand hand)
    {
        if (hand == null)
        {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (swingConfig.getValue())
        {
            mc.player.swingHand(hand);
        }
        else
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }

        // Entity ID predict
        if (idPredictConfig.getValue())
        {
            boolean flag = AutoXPModule.getInstance().isEnabled() || mc.player.isUsingItem() && mc.player.getStackInHand(mc.player.getActiveHand()).getItem() instanceof ExperienceBottleItem;
            int id = (int) (predictId + 1);
            if (flag || attackPackets.containsKey(id))
            {
                return;
            }
            Entity entity = mc.world.getEntityById(id);
            if (entity != null && !(entity instanceof EndCrystalEntity))
            {
                return;
            }
            EndCrystalEntity entity2 = new EndCrystalEntity(mc.world, 0.0, 0.0, 0.0);
            entity2.setId(id);
            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity2, false);
            Managers.NETWORK.sendPacket(packet);
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            attackPackets.put(id, System.currentTimeMillis());
        }
    }

    private boolean isSilentSwap(Swap swap)
    {
        return swap == Swap.SILENT || swap == Swap.SILENT_ALT;
    }

    private int getCrystalSlot()
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof EndCrystalItem)
            {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private Direction getPlaceDirection(BlockPos blockPos)
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        if (strictDirectionConfig.getValue())
        {
            if (mc.player.getY() >= blockPos.getY())
            {
                return Direction.UP;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK)
            {
                return result.getSide();
            }
        }
        else
        {
            if (mc.world.isInBuildLimit(blockPos))
            {
                return Direction.DOWN;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK)
            {
                return result.getSide();
            }
        }
        return Direction.UP;
    }

    private DamageData<EndCrystalEntity> calculateAttackCrystal(List<Entity> entities)
    {
        if (entities.isEmpty())
        {
            return null;
        }

        final List<DamageData<EndCrystalEntity>> validData = new ArrayList<>();

        DamageData<EndCrystalEntity> data = null;
        for (Entity crystal : entities)
        {
            if (!(crystal instanceof EndCrystalEntity crystal1) || !crystal.isAlive()
                    || stuckCrystals.stream().anyMatch(d -> d.id() == crystal.getId()))
            {
                continue;
            }
            Long time = attackPackets.get(crystal.getId());
            boolean attacked = time != null && time < getBreakMs();
            if ((crystal.age < ticksExistedConfig.getValue() || attacked) && inhibitConfig.getValue())
            {
                continue;
            }
            if (attackRangeCheck(crystal1))
            {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystal.getPos(),
                    blockDestructionConfig.getValue(), selfExtrapolateConfig.getValue() ? extrapolateTicksConfig.getValue() : 0, false);
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.getValue())
            {
                continue;
            }
            for (Entity entity : entities)
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || Managers.SOCIAL.isFriend(entity.getName()))
                {
                    continue;
                }
                double crystalDist = crystal.squaredDistanceTo(entity);
                if (crystalDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue())
                {
                    continue;
                }

                boolean antiSurround = false;
                if (antiSurroundConfig.getValue() && entity instanceof PlayerEntity player
                        && !BlastResistantBlocks.isUnbreakable(player.getBlockPos()))
                {
                    Set<BlockPos> miningPositions = new HashSet<>();
                    BlockPos miningBlock = AutoMineModule.getInstance().getMiningBlock();
                    if (AutoMineModule.getInstance().isEnabled() && miningBlock != null)
                    {
                        miningPositions.add(miningBlock);
                    }
                    if (Managers.BLOCK.getMines(0.75f).contains(player.getBlockPos().up()))
                    {
                        miningPositions.add(player.getBlockPos().up());
                    }
                    for (BlockPos miningBlockPos : miningPositions)
                    {
                        if (!SurroundModule.getInstance().getSurroundNoDown(player).contains(miningBlockPos))
                        {
                            continue;
                        }
                        for (Direction direction : Direction.values())
                        {
                            BlockPos pos1 = miningBlockPos.offset(direction);
                            if (crystal.getBlockPos().equals(pos1.down()))
                            {
                                antiSurround = true;
                            }
                        }
                    }
                }

                double damage = ExplosionUtil.getDamageTo(entity, crystal.getPos(), blockDestructionConfig.getValue(),
                        extrapolateTicksConfig.getValue(), assumeArmorConfig.getValue());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity))
                {
                    continue;
                }

                DamageData<EndCrystalEntity> currentData = new DamageData<>(crystal1, entity,
                        damage, selfDamage, crystal1.getBlockPos().down(), antiSurround);
                validData.add(currentData);
                if (data == null || damage > data.getDamage())
                {
                    data = currentData;
                }
            }
        }
        if (data == null || targetDamageCheck(data))
        {
            if (antiSurroundConfig.getValue())
            {
                return validData.stream()
                        .filter(DamageData::isAntiSurround)
                        .min(Comparator.comparingDouble(d -> mc.player.squaredDistanceTo(d.getBlockPos().toCenterPos())))
                        .orElse(null);
            }
            return null;
        }
        return data;
    }

    private boolean attackRangeCheck(EndCrystalEntity entity)
    {
        return attackRangeCheck(entity.getPos());
    }

    /**
     * @param entityPos
     * @return
     */
    private boolean attackRangeCheck(Vec3d entityPos)
    {
        double breakRange = breakRangeConfig.getValue();
        double breakWallRange = breakWallRangeConfig.getValue();
        Vec3d playerPos = mc.player.getEyePos();
        double dist = playerPos.squaredDistanceTo(entityPos);
        if (dist > breakRange * breakRange)
        {
            return true;
        }
        double yOff = Math.abs(entityPos.getY() - mc.player.getY());
        if (yOff > maxYOffsetConfig.getValue())
        {
            return true;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                playerPos, entityPos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() != HitResult.Type.MISS && dist > breakWallRange * breakWallRange;
    }

    private DamageData<BlockPos> calculatePlaceCrystal(List<BlockPos> placeBlocks, List<Entity> entities)
    {
        if (placeBlocks.isEmpty() || entities.isEmpty())
        {
            return null;
        }

        final List<DamageData<BlockPos>> validData = new ArrayList<>();

        DamageData<BlockPos> data = null;
        for (BlockPos pos : placeBlocks)
        {
            if (!canUseCrystalOnBlock(pos) || placeRangeCheck(pos) || intersectingAntiStuckCheck(pos))
            {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalDamageVec(pos),
                    blockDestructionConfig.getValue(), selfExtrapolateConfig.getValue() ? extrapolateTicksConfig.getValue() : 0, false);
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.getValue())
            {
                continue;
            }
            for (Entity entity : entities)
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || Managers.SOCIAL.isFriend(entity.getName()))
                {
                    continue;
                }
                double blockDist = pos.getSquaredDistance(entity.getPos());
                if (blockDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue())
                {
                    continue;
                }

                boolean antiSurround = false;
                if (antiSurroundConfig.getValue() && entity instanceof PlayerEntity player
                        && !BlastResistantBlocks.isUnbreakable(player.getBlockPos()))
                {
                    Set<BlockPos> miningPositions = new HashSet<>();
                    BlockPos miningBlock = AutoMineModule.getInstance().getMiningBlock();
                    if (AutoMineModule.getInstance().isEnabled() && miningBlock != null)
                    {
                        miningPositions.add(miningBlock);
                    }
                    if (Managers.BLOCK.getMines(0.75f).contains(player.getBlockPos().up()))
                    {
                        miningPositions.add(player.getBlockPos().up());
                    }
                    for (BlockPos miningBlockPos : miningPositions)
                    {
                        if (!SurroundModule.getInstance().getSurroundNoDown(player).contains(miningBlockPos))
                        {
                            continue;
                        }
                        for (Direction direction : Direction.values())
                        {
                            BlockPos pos1 = miningBlockPos.offset(direction);
                            if (pos.equals(pos1.down()))
                            {
                                antiSurround = true;
                            }
                        }
                    }
                }

                double damage;
                damage = ExplosionUtil.getDamageTo(entity, crystalDamageVec(pos), blockDestructionConfig.getValue(),
                        extrapolateTicksConfig.getValue(), assumeArmorConfig.getValue());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity))
                {
                    continue;
                }

                DamageData<BlockPos> currentData = new DamageData<>(pos, entity,
                        damage, selfDamage, antiSurround);
                validData.add(currentData);
                if (data == null || damage > data.getDamage())
                {
                    data = currentData;
                }
            }
        }
        if (data == null || targetDamageCheck(data))
        {
            if (antiSurroundConfig.getValue())
            {
                return validData.stream()
                        .filter(DamageData::isAntiSurround)
                        .min(Comparator.comparingDouble(d -> mc.player.squaredDistanceTo(d.getBlockPos().toCenterPos())))
                        .orElse(null);
            }
            return null;
        }
        return data;
    }

    /**
     * @param pos
     * @return
     */
    private boolean placeRangeCheck(BlockPos pos)
    {
        double placeRange = placeRangeConfig.getValue();
        double placeWallRange = placeWallRangeConfig.getValue();
        Vec3d player = placeRangeEyeConfig.getValue() ? mc.player.getEyePos() : mc.player.getPos();
        double dist = placeRangeCenterConfig.getValue() ?
                player.squaredDistanceTo(pos.toCenterPos()) : pos.getSquaredDistance(player.x, player.y, player.z);
        if (dist > placeRange * placeRange)
        {
            return true;
        }
        Vec3d raytrace = Vec3d.of(pos).add(0.5, 2.70000004768372, 0.5);
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                mc.player.getEyePos(), raytrace,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        float maxDist = breakRangeConfig.getValue() * breakRangeConfig.getValue();
        if (result != null && result.getType() == HitResult.Type.BLOCK && !result.getBlockPos().equals(pos))
        {
            maxDist = breakWallRangeConfig.getValue() * breakWallRangeConfig.getValue();
            if (!raytraceConfig.getValue() || dist > placeWallRange * placeWallRange)
            {
                return true;
            }
        }
        return breakValidConfig.getValue() && dist > maxDist;
    }

    public void placeCrystalForTarget(PlayerEntity target, BlockPos blockPos)
    {
        if (target == null || target.isDead() || placeRangeCheck(blockPos) || !canUseCrystalOnBlock(blockPos))
        {
            return;
        }
        double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalDamageVec(blockPos),
                blockDestructionConfig.getValue(), Set.of(blockPos), selfExtrapolateConfig.getValue() ? extrapolateTicksConfig.getValue() : 0, false);
        if (playerDamageCheck(selfDamage))
        {
            return;
        }
        double damage = ExplosionUtil.getDamageTo(target, crystalDamageVec(blockPos), blockDestructionConfig.getValue(),
                Set.of(blockPos), extrapolateTicksConfig.getValue(), assumeArmorConfig.getValue());
        if (damage < minDamageConfig.getValue() && !isCrystalLethalTo(damage, target)
                || placeCrystal != null && placeCrystal.getDamage() >= damage)
        {
            return;
        }

        float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), blockPos.toCenterPos());
        setRotation(rotations[0], rotations[1]);
        placeCrystal(blockPos, Hand.MAIN_HAND, false);
        fadeList.put(blockPos, new Animation(true, fadeTimeConfig.getValue()));
    }

    private boolean checkOverrideSafety(boolean unsafeToPlayer, double damage, Entity entity)
    {
        return safetyOverride.getValue() && unsafeToPlayer && damage < EntityUtil.getHealth(entity) + 0.5;
    }

    private boolean targetDamageCheck(DamageData<?> crystal)
    {
        double minDmg = minDamageConfig.getValue();
        if (crystal.getAttackTarget() instanceof LivingEntity entity && isCrystalLethalTo(crystal, entity))
        {
            minDmg = 2.0f;
        }
        return crystal.getDamage() < minDmg;
    }

    private boolean playerDamageCheck(double playerDamage)
    {
        if (!mc.player.isCreative())
        {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (safetyConfig.getValue() && playerDamage >= health + 0.5f)
            {
                return true;
            }
            return playerDamage > maxLocalDamageConfig.getValue();
        }
        return false;
    }

    private boolean isFeetSurrounded(LivingEntity entity)
    {
        BlockPos pos1 = entity.getBlockPos();
        if (!mc.world.getBlockState(pos1).isReplaceable())
        {
            return true;
        }
        for (Direction direction : Direction.values())
        {
            if (!direction.getAxis().isHorizontal())
            {
                continue;
            }
            BlockPos pos2 = pos1.offset(direction);
            if (mc.world.getBlockState(pos2).isReplaceable())
            {
                return false;
            }
        }
        return true;
    }

    private boolean checkAntiTotem(double damage, LivingEntity entity)
    {
        if (entity instanceof PlayerEntity p)
        {
            float phealth = EntityUtil.getHealth(p);
            if (phealth <= 2.0f && phealth - damage < 0.5f)
            {
                long time = Managers.TOTEM.getLastPopTime(p);
                if (time != -1)
                {
                    return System.currentTimeMillis() - time <= 500;
                }
            }
        }
        return false;
    }

    private boolean isCrystalLethalTo(DamageData<?> crystal, LivingEntity entity)
    {
        return isCrystalLethalTo(crystal.getDamage(), entity);
    }

    private boolean isCrystalLethalTo(double damage, LivingEntity entity)
    {
        if (lethalDamageConfig.getValue() && lastAttackTimer.passed(500))
        {
            return true;
        }

        if (antiTotemConfig.getValue() && checkAntiTotem(damage, entity))
        {
            return true;
        }
        float health = entity.getHealth() + entity.getAbsorptionAmount();
        if (damage * (1.0f + lethalMultiplier.getValue()) >= health + 0.5f)
        {
            return true;
        }
        if (armorBreakerConfig.getValue())
        {
            for (ItemStack armorStack : entity.getArmorItems())
            {
                int n = armorStack.getDamage();
                int n1 = armorStack.getMaxDamage();
                float durability = ((n1 - n) / (float) n1) * 100.0f;
                if (durability < armorScaleConfig.getValue())
                {
                    return true;
                }
            }
        }

        // Antiregear
        if (shulkersConfig.getValue() && entity instanceof PlayerEntity)
        {
            for (BlockPos pos : getSphere(3.0f, entity.getPos()))
            {
                BlockState state = mc.world.getBlockState(pos);
                if (state.getBlock() instanceof ShulkerBoxBlock)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attackCheckPre(Hand hand)
    {
        if (!lastSwapTimer.passed(swapDelayConfig.getValue() * 25.0f))
        {
            return true;
        }
        if (hand == Hand.MAIN_HAND)
        {
            return checkCanUseCrystal();
        }
        return false;
    }

    private boolean checkCanUseCrystal()
    {
        return !multitaskConfig.getValue() && checkMultitask()
                || !whileMiningConfig.getValue() && mc.interactionManager.isBreakingBlock();
    }

    private boolean isHoldingCrystal()
    {
        if (!checkCanUseCrystal() && (autoSwapConfig.getValue() == Swap.SILENT || autoSwapConfig.getValue() == Swap.SILENT_ALT))
        {
            return true;
        }
        return getCrystalHand() != null;
    }

    private Vec3d crystalDamageVec(BlockPos pos)
    {
        return Vec3d.of(pos).add(0.5, 1.0, 0.5);
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     */
    private boolean isValidTarget(Entity e)
    {
        return e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }

    /**
     * Returns <tt>true</tt> if an {@link EndCrystalItem} can be used on the
     * param {@link BlockPos}.
     *
     * @param pos The block pos
     * @return Returns <tt>true</tt> if the crystal item can be placed on the
     * block
     */
    public boolean canUseCrystalOnBlock(BlockPos pos)
    {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return false;
        }
        return isCrystalHitboxClear(pos);
    }

    public boolean isCrystalHitboxClear(BlockPos pos)
    {
        BlockPos p2 = pos.up();
        BlockState state2 = mc.world.getBlockState(p2);
        // ver 1.12.2 and below
        if (placementsConfig.getValue() == Placements.PROTOCOL && !mc.world.isAir(p2.up()))
        {
            return false;
        }
        if (!mc.world.isAir(p2) && !state2.isOf(Blocks.FIRE))
        {
            return false;
        }
        else
        {
            final Box bb = Managers.NETWORK.isCrystalPvpCC() ? HALF_CRYSTAL_BB : FULL_CRYSTAL_BB;
            double d = p2.getX();
            double e = p2.getY();
            double f = p2.getZ();
            List<Entity> list = getEntitiesBlockingCrystal(new Box(d, e, f,
                    d + bb.maxX, e + bb.maxY, f + bb.maxZ));
            return list.isEmpty();
        }
    }

    private List<Entity> getEntitiesBlockingCrystal(Box box)
    {
        List<Entity> entities = new CopyOnWriteArrayList<>(
                mc.world.getOtherEntities(null, box));
        //
        for (Entity entity : entities)
        {
            if (entity == null || !entity.isAlive()
                    || entity instanceof ExperienceOrbEntity
                    || forcePlaceConfig.getValue() != ForcePlace.NONE
                    && entity instanceof ItemEntity && entity.age <= 10)
            {
                entities.remove(entity);
            }
            else if (entity instanceof EndCrystalEntity entity1
                    && entity1.getBoundingBox().intersects(box))
            {
                Integer antiStuckAttacks = antiStuckCrystals.get(entity1.getId());
                if (!attackRangeCheck(entity1) && (antiStuckAttacks == null || antiStuckAttacks <= attackLimitConfig.getValue() * 10.0f))
                {
                    entities.remove(entity);
                }
                else
                {
                    double dist = mc.player.squaredDistanceTo(entity1);
                    stuckCrystals.add(new AntiStuckData(entity1.getId(), entity1.getBlockPos(), entity1.getPos(), dist));
                }
            }
        }
        return entities;
    }

    private boolean intersectingAntiStuckCheck(BlockPos blockPos)
    {
        if (stuckCrystals.isEmpty())
        {
            return false;
        }
        return stuckCrystals.stream().anyMatch(d -> d.blockPos().equals(blockPos.up()));
    }

    private EndCrystalEntity intersectingCrystalCheck(BlockPos pos)
    {
        return (EndCrystalEntity) mc.world.getOtherEntities(null, new Box(pos)).stream()
                .filter(e -> e instanceof EndCrystalEntity).min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        double rad = Math.ceil(placeRangeConfig.getValue());
        return getSphere(rad, origin);
    }

    private List<BlockPos> getSphere(double rad, Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    private boolean canHoldCrystal()
    {
        return isHoldingCrystal() || autoSwapConfig.getValue() != Swap.OFF && getCrystalSlot() != -1;
    }

    private Hand getCrystalHand()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        final ItemStack mainhand = mc.player.getMainHandStack();
        if (offhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.OFF_HAND;
        }
        else if (mainhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public float getBreakDelay()
    {
        return 1000.0f - breakSpeedConfig.getValue() * 50.0f;
    }

    // Debug info
    public void setStage(String crystalStage)
    {
        // this.crystalStage = crystalStage;
    }

    public int getBreakMs()
    {
        if (attackLatency.isEmpty())
        {
            return 0;
        }
        float avg = 0.0f;
        // fix ConcurrentModificationException
        ArrayList<Long> latencyCopy = Lists.newArrayList(attackLatency);
        if (!latencyCopy.isEmpty())
        {
            for (float t : latencyCopy)
            {
                avg += t;
            }
            avg /= latencyCopy.size();
        }
        return (int) avg;
    }

    public boolean shouldPreForcePlace()
    {
        return forcePlaceConfig.getValue() == ForcePlace.PRE;
    }

    public float getPlaceRange()
    {
        return placeRangeConfig.getValue();
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Sequential
    {
        NORMAL,
        STRICT,
        NONE
    }

    public enum ForcePlace
    {
        PRE,
        POST,
        NONE
    }

    public enum Placements
    {
        NATIVE,
        PROTOCOL
    }

    public enum Rotate
    {
        FULL,
        SEMI,
        OFF
    }

    private record AntiStuckData(int id, BlockPos blockPos, Vec3d pos, double stuckDist) {}

    private static class DamageData<T>
    {
        //
        private final List<String> tags = new ArrayList<>();
        private T damageData;
        private Entity attackTarget;
        private BlockPos blockPos;
        //
        private double damage, selfDamage;
        private boolean antiSurround;

        //
        public DamageData()
        {

        }

        public DamageData(BlockPos damageData, Entity attackTarget, double damage,
                          double selfDamage, boolean antiSurround)
        {
            this.damageData = (T) damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = damageData;
            this.antiSurround = antiSurround;
        }

        public DamageData(T damageData, Entity attackTarget, double damage,
                          double selfDamage, BlockPos blockPos, boolean antiSurround)
        {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = blockPos;
            this.antiSurround = antiSurround;
        }

        public void setDamageData(T damageData, Entity attackTarget, double damage, double selfDamage)
        {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
        }

        public T getDamageData()
        {
            return damageData;
        }

        public Entity getAttackTarget()
        {
            return attackTarget;
        }

        public double getDamage()
        {
            return damage;
        }

        public double getSelfDamage()
        {
            return selfDamage;
        }

        public BlockPos getBlockPos()
        {
            return blockPos;
        }

        public boolean isAntiSurround()
        {
            return antiSurround;
        }
    }

    private class AttackCrystalTask implements Callable<DamageData<EndCrystalEntity>>
    {
        private final List<Entity> threadSafeEntities;

        public AttackCrystalTask(List<Entity> threadSafeEntities)
        {
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<EndCrystalEntity> call() throws Exception
        {
            return calculateAttackCrystal(threadSafeEntities);
        }
    }

    private class PlaceCrystalTask implements Callable<DamageData<BlockPos>>
    {
        private final List<BlockPos> threadSafeBlocks;
        private final List<Entity> threadSafeEntities;

        public PlaceCrystalTask(List<BlockPos> threadSafeBlocks,
                                List<Entity> threadSafeEntities)
        {
            this.threadSafeBlocks = threadSafeBlocks;
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<BlockPos> call() throws Exception
        {
            return calculatePlaceCrystal(threadSafeBlocks, threadSafeEntities);
        }
    }
}
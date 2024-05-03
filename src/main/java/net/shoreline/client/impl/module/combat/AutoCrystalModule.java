package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
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
import net.shoreline.client.api.event.handler.EventBus;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.RunTickEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.EvictingQueue;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.animation.TimeAnimation;
import net.shoreline.client.util.world.EndCrystalUtil;
import net.shoreline.client.util.world.EntityUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author linus
 * @since 1.0
 */
public class AutoCrystalModule extends RotationModule {

    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows attacking while using items", false);
    Config<Boolean> whileMiningConfig = new BooleanConfig("WhileMining", "Allows attacking while mining blocks", false);
    Config<Float> targetRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for potential enemies", 1.0f, 10.0f, 13.0f);
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Instantly attacks crystals when they spawn", false);
    Config<Boolean> instantCalcConfig = new BooleanConfig("Instant-Calc", "Calculates a crystal when it spawns and attacks if it meets MINIMUM requirements, this will result in non-ideal crystal attacks", false, () -> instantConfig.getValue());
    Config<Float> instantDamageConfig = new NumberConfig<>("InstantDamage", "Minimum damage to attack crystals instantly", 1.0f, 6.0f, 10.0f, () -> instantConfig.getValue() && instantCalcConfig.getValue());
    Config<Boolean> instantMaxConfig = new BooleanConfig("InstantMax", "Attacks crystals instantly if they exceed the previous max attack damage (Note: This is still not a perfect check because the next tick could have better damages)", true, () -> instantConfig.getValue());
    Config<Boolean> raytraceConfig = new BooleanConfig("Raytrace", "Raytrace to crystal position", true);
    Config<Boolean> swingConfig = new BooleanConfig("Swing", "Swing hand when placing and attacking crystals", true);
    // ROTATE SETTINGS
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before placing and breaking", false);
    // Config<Boolean> rotateSilentConfig = new BooleanConfig("RotateSilent", "Silently updates rotations to server", false);
    Config<Rotate> strictRotateConfig = new EnumConfig<>("YawStep", "Rotates yaw over multiple ticks to prevent certain rotation flags in NCP", Rotate.OFF, Rotate.values(), () -> rotateConfig.getValue());
    Config<Integer> rotateLimitConfig = new NumberConfig<>("YawStep-Limit", "Maximum yaw rotation in degrees for one tick", 1, 180, 180, NumberDisplay.DEGREES, () -> rotateConfig.getValue() && strictRotateConfig.getValue() != Rotate.OFF);
    // Config<Boolean> rotateTickFactorConfig = new BooleanConfig("Rotate-TickReduction", "Factors in angles when calculating crystals", false, () -> rotateConfig.getValue() && strictRotateConfig.getValue() != Rotate.OFF);

    Config<Boolean> playersConfig = new BooleanConfig("Players", "Target players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Target monsters", false);
    Config<Boolean> neutralsConfig = new BooleanConfig("Neutrals", "Target neutrals", false);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Target animals", false);
    // BREAK SETTINGS
    Config<Float> breakSpeedConfig = new NumberConfig<>("BreakSpeed", "Speed to break crystals", 0.1f, 18.0f, 20.0f);
    Config<Float> attackDelayConfig = new NumberConfig<>("AttackDelay", "Added delays", 0.0f, 0.0f, 5.0f);
    Config<Integer> attackFactorConfig = new NumberConfig<>("AttackFactor", "Factor of attack delay", 0, 0, 3, () -> attackDelayConfig.getValue() > 0.0);
    Config<Float> randomSpeedConfig = new NumberConfig<>("RandomSpeed", "Randomized delay for breaking crystals", 0.0f, 0.0f, 10.0f);
    Config<Boolean> breakDelayConfig = new BooleanConfig("BreakDelay", "Uses attack latency to calculate break delays", false);
    Config<Float> breakTimeoutConfig = new NumberConfig<>("BreakTimeout", "Time after waiting for the average break time before considering a crystal attack failed", 0.0f, 3.0f, 10.0f, () -> breakDelayConfig.getValue());
    Config<Float> minTimeoutConfig = new NumberConfig<>("MinTimeout", "Minimum time before considering a crystal break/place failed", 0.0f, 5.0f, 20.0f, () -> breakDelayConfig.getValue());
    Config<Integer> ticksExistedConfig = new NumberConfig<>("TicksExisted", "Minimum ticks alive to consider crystals for attack", 0, 0, 10);
    Config<Float> breakRangeConfig = new NumberConfig<>("BreakRange", "Range to break crystals", 0.1f, 4.0f, 5.0f);
    Config<Float> strictBreakRangeConfig = new NumberConfig<>("StrictBreakRange", "NCP range to break crystals", 0.1f, 4.0f, 5.0f);
    Config<Float> maxYOffsetConfig = new NumberConfig<>("MaxYOffset", "Maximum crystal y-offset difference", 1.0f, 5.0f, 10.0f);
    Config<Float> breakWallRangeConfig = new NumberConfig<>("BreakWallRange", "Range to break crystals through walls", 0.1f, 4.0f, 5.0f);
    Config<Swap> antiWeaknessConfig = new EnumConfig<>("AntiWeakness", "Swap to tools before attacking crystals", Swap.OFF, Swap.values());
    Config<Float> swapDelayConfig = new NumberConfig<>("SwapPenalty", "Delay for attacking after swapping items which prevents NCP flags", 0.0f, 0.0f, 10.0f);
    // fight.speed:
    //        limit: 13
    // shortterm:
    //        ticks: 8
    Config<Boolean> inhibitConfig = new BooleanConfig("Inhibit", "Prevents excessive attacks", true);
    // default NCP config
    // limitforseconds:
    //        half: 8
    //        one: 15
    //        two: 30
    //        four: 60
    //        eight: 100
    Config<Boolean> manualConfig = new BooleanConfig("ManualCrystal", "Always breaks manually placed crystals", false);
    Config<Boolean> placeConfig = new BooleanConfig("Place", "Places crystals to damage enemies. Place settings will only function if this setting is enabled.", true);
    Config<Float> placeSpeedConfig = new NumberConfig<>("PlaceSpeed", "Speed to place crystals", 0.1f, 18.0f, 20.0f, () -> placeConfig.getValue());
    Config<Float> placeRangeConfig = new NumberConfig<>("PlaceRange", "Range to place crystals", 0.1f, 4.0f, 5.0f, () -> placeConfig.getValue());
    Config<Float> strictPlaceRangeConfig = new NumberConfig<>("StrictPlaceRange", "NCP range to place crystals", 0.1f, 4.0f, 5.0f, () -> placeConfig.getValue());
    Config<Float> placeWallRangeConfig = new NumberConfig<>("PlaceWallRange", "Range to place crystals through walls", 0.1f, 4.0f, 5.0f, () -> placeConfig.getValue());
    Config<Boolean> placeRangeEyeConfig = new BooleanConfig("PlaceRangeEye", "Calculates place ranges starting from the eye position of the player", false, () -> placeConfig.getValue());
    Config<Boolean> placeRangeCenterConfig = new BooleanConfig("PlaceRangeCenter", "Calculates place ranges to the center of the block", true, () -> placeConfig.getValue());
    Config<Boolean> antiTotemConfig = new BooleanConfig("AntiTotem", "Predicts totems and places crystals to instantly double pop and kill the target", false, () -> placeConfig.getValue());
    Config<Swap> autoSwapConfig = new EnumConfig<>("Swap", "Swaps to an end crystal before placing if the player is not holding one", Swap.OFF, Swap.values(), () -> placeConfig.getValue());
    Config<Float> alternateSpeedConfig = new NumberConfig<>("AlternateSpeed", "Speed for alternative swapping crystals", 1.0f, 18.0f, 20.0f, () -> placeConfig.getValue() && autoSwapConfig.getValue() == Swap.SILENT_ALT);
    Config<Boolean> antiSurroundConfig = new BooleanConfig("AntiSurround", "Places on mining blocks that when broken, can be placed on to damage enemies. Instantly destroys items spawned from breaking block and allows faster placing", false, () -> placeConfig.getValue());
    Config<Boolean> breakValidConfig = new BooleanConfig("Strict", "Only places crystals that can be attacked", false, () -> placeConfig.getValue());
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Interacts with only visible directions when placing crystals", false, () -> placeConfig.getValue());
    Config<Boolean> exposedDirectionConfig = new BooleanConfig("StrictDirection-Exposed", "Interacts with only exposed directions when placing crystals", false, () -> placeConfig.getValue());
    Config<Placements> placementsConfig = new EnumConfig<>("Placements", "Version standard for placing end crystals", Placements.NATIVE, Placements.values(), () -> placeConfig.getValue());
    Config<Float> minDamageConfig = new NumberConfig<>("MinDamage", "Minimum damage required to consider attacking or placing an end crystal", 1.0f, 4.0f, 10.0f);
    Config<Boolean> armorBreakerConfig = new BooleanConfig("ArmorBreaker", "Attempts to break enemy armor with crystals", true);
    Config<Float> armorScaleConfig = new NumberConfig<>("ArmorScale", "Armor damage scale before attempting to break enemy armor with crystals", 1.0f, 5.0f, 20.0f, NumberDisplay.PERCENT, () -> armorBreakerConfig.getValue());
    Config<Float> lethalMultiplier = new NumberConfig<>("LethalMultiplier", "If we can kill an enemy with this many crystals, disregard damage values", 0.0f, 1.5f, 4.0f);
    Config<Boolean> lethalDamageConfig = new BooleanConfig("Lethal-DamageTick", "Places lethal crystals on ticks where they damage entities", false);
    Config<Boolean> safetyConfig = new BooleanConfig("Safety", "Accounts for total player safety when attacking and placing crystals", true);
    Config<Boolean> safetyOverride = new BooleanConfig("SafetyOverride", "Overrides the safety checks if the crystal will kill an enemy", false);
    Config<Float> maxLocalDamageConfig = new NumberConfig<>("MaxLocalDamage", "The maximum player damage", 4.0f, 12.0f, 20.0f);
    Config<Boolean> blockDestructionConfig = new BooleanConfig("BlockDestruction", "Accounts for explosion block destruction when calculating damages", false);
    Config<Boolean> extrapolateRangeConfig = new BooleanConfig("ExtrapolateRange", "Accounts for motion when calculating ranges", false);
    Config<Integer> extrapolateTicksConfig = new NumberConfig<>("ExtrapolationTicks", "Accounts for motion when calculating enemy positions, not fully accurate.", 0, 0, 10);
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders the current placement", true);
    Config<Boolean> fadeConfig = new BooleanConfig("Fade", "Fades old renders out", true, () -> renderConfig.getValue());
    Config<Integer> fadeTimeConfig = new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 250, 1000, () -> false);
    Config<Boolean> damageNametagConfig = new BooleanConfig("Render-Damage", "Renders the current expected damage of a place/attack", false, () -> renderConfig.getValue());
    Config<Boolean> breakDebugConfig = new BooleanConfig("Break-Debug", "Debugs break ms in data", false, () -> renderConfig.getValue());
    //
    Config<Boolean> disableDeathConfig = new BooleanConfig("DisableOnDeath", "Disables during disconnect/death", false);
    //
    private DamageData<EndCrystalEntity> attackCrystal;
    private DamageData<BlockPos> placeCrystal;
    //
    private BlockPos renderPos;
    private BlockPos renderSpawnPos;
    //
    private Vec3d crystalRotation;
    private boolean attackRotate;
    private boolean rotated;
    private float[] silentRotations;
    //
    private static final Box FULL_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);
    private static final Box HALF_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private final Timer lastAttackTimer = new CacheTimer();
    private final Timer lastPlaceTimer = new CacheTimer();
    private final Timer lastSwapTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    //
    private final ArrayDeque<Long> attackLatency = new EvictingQueue<>(20);
    private final List<BlockPos> manualCrystals = new ArrayList<>();
    private final Map<Integer, Long> attackPackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> placePackets =
            Collections.synchronizedMap(new ConcurrentHashMap<>());

    //gonna make a better solution for this in the future.
    private final Map<BlockPos, TimeAnimation> fadeBoxes = new HashMap<>();
    private final Map<BlockPos, TimeAnimation> fadeLines = new HashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public AutoCrystalModule() {
        super("AutoCrystal", "Attacks entities with end crystals",
                ModuleCategory.COMBAT, 750);
    }

    @Override
    public String getModuleData() {
        if (breakDebugConfig.getValue()) {
            return String.format("%dms", getBreakMs());
        }
        return super.getModuleData();
    }

    @Override
    public void onDisable() {
        renderPos = null;
        attackCrystal = null;
        placeCrystal = null;
        crystalRotation = null;
        silentRotations = null;
        attackPackets.clear();
        placePackets.clear();
        setStage("NONE");
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        if (disableDeathConfig.getValue()) {
            disable();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event) {
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
                || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed()) {
            autoSwapTimer.reset();
        }
        renderPos = null;
        ArrayList<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        List<BlockPos> blocks = getSphere(mc.player.getPos());
        attackCrystal = calculateAttackCrystal(entities);
        if (placeConfig.getValue()) {
            placeCrystal = calculatePlaceCrystal(blocks, entities);
        }
        float breakDelay = 1000.0f - breakSpeedConfig.getValue() * 50.0f;
        if (breakDelayConfig.getValue()) {
            breakDelay = Math.max(minTimeoutConfig.getValue() * 50.0f, getBreakMs() + breakTimeoutConfig.getValue() * 50.0f);
        }
        attackRotate = attackCrystal != null && attackDelayConfig.getValue() <= 0.0 && lastAttackTimer.passed(breakDelay);
        if (attackCrystal != null) {
            crystalRotation = attackCrystal.damageData.getPos();
        } else if (placeCrystal != null) {
            crystalRotation = placeCrystal.damageData.toCenterPos().add(0.0, 0.5, 0.0);
        }
        if (rotateConfig.getValue() && crystalRotation != null && (placeCrystal == null || canHoldCrystal())) {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalRotation);
            if (strictRotateConfig.getValue() == Rotate.FULL || strictRotateConfig.getValue() == Rotate.SEMI && attackRotate) {
                float yaw;
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotations[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f) {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimitConfig.getValue();
                if (diff1 > rotateLimitConfig.getValue()) {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                } else {
                    yaw = rotations[0];
                    rotated = true;
                    crystalRotation = null;
                }
                rotations[0] = yaw;
            } else {
                rotated = true;
                crystalRotation = null;
            }
            setRotation(rotations[0], rotations[1]);
        } else {
            silentRotations = null;
        }
        if (isRotationBlocked() || !rotated && rotateConfig.getValue()) {
            return;
        }
//        if (rotateSilentConfig.getValue() && silentRotations != null) {
//            setRotationSilent(silentRotations[0], silentRotations[1]);
//        }
        final Hand hand = getCrystalHand();
        if (attackCrystal != null) {
            // ChatUtil.clientSendMessage("yaw: " + rotations[0] + ", pitch: " + rotations[1]);
            if (attackRotate) {
                // ChatUtil.clientSendMessage("break range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(attackCrystal.getDamageData().getPos())));
                attackCrystal(attackCrystal.getDamageData(), hand);
                setStage("ATTACKING");
                lastAttackTimer.reset();
            }
        }
        if (placeCrystal != null) {
            renderPos = placeCrystal.getDamageData();
            if (lastPlaceTimer.passed(1000.0f - placeSpeedConfig.getValue() * 50.0f)) {
                // ChatUtil.clientSendMessage("place range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(placeCrystal.getDamageData().toCenterPos())));
                placeCrystal(placeCrystal.getDamageData(), hand);
                setStage("PLACING");
                lastPlaceTimer.reset();
            }
        }
    }

    @EventListener
    public void onRunTick(RunTickEvent event) {
        if (mc.player == null || attackDelayConfig.getValue() <= 0.0) {
            return;
        }
        float attackFactor = 50.0f / Math.max(1.0f, attackFactorConfig.getValue());
        if (attackCrystal != null && lastAttackTimer.passed(attackDelayConfig.getValue() * attackFactor)) {
            attackCrystal(attackCrystal.getDamageData(), getCrystalHand());
            lastAttackTimer.reset();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            if (fadeConfig.getValue())
            {
                for (Map.Entry<BlockPos, TimeAnimation> set : fadeBoxes.entrySet())
                {
                    if (set.getKey() == renderPos)
                    {
                        continue;
                    }

                    set.getValue().setState(false);
                    int alpha = (int) set.getValue().getCurrent();
                    Color color = Modules.COLORS.getColor(alpha);
                    RenderManager.renderBox(event.getMatrices(), set.getKey(), color.getRGB());
                }

                for (Map.Entry<BlockPos, TimeAnimation> set : fadeLines.entrySet())
                {
                    if (set.getKey() == renderPos)
                    {
                        continue;
                    }

                    set.getValue().setState(false);
                    int alpha = (int) set.getValue().getCurrent();
                    Color color = Modules.COLORS.getColor(alpha);
                    RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, color.getRGB());
                }
            }

            if (renderPos != null && isHoldingCrystal())
            {
                if (!fadeConfig.getValue())
                {
                    RenderManager.renderBox(event.getMatrices(), renderPos, Modules.COLORS.getRGB(80));
                    RenderManager.renderBoundingBox(event.getMatrices(), renderPos, 1.5f,
                            Modules.COLORS.getRGB(145));

                    if (damageNametagConfig.getValue() && placeCrystal != null) {
                        DecimalFormat format = new DecimalFormat("0.0");
                        RenderManager.post(() -> {
                            RenderManager.renderSign(event.getMatrices(),
                                    format.format(placeCrystal.getDamage()), renderPos.toCenterPos());
                        });
                    }
                }
                else
                {
                    TimeAnimation boxAnimation = new TimeAnimation(true, 0, 80, fadeTimeConfig.getValue());
                    TimeAnimation lineAnimation = new TimeAnimation(true, 0, 145, fadeTimeConfig.getValue());
                    fadeBoxes.put(renderPos, boxAnimation);
                    fadeLines.put(renderPos, lineAnimation);
                }
            }

            fadeBoxes.entrySet().removeIf(e ->
                    e.getValue().getFactor() == 0.0);
            fadeLines.entrySet().removeIf(e ->
                    e.getValue().getFactor() == 0.0);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof PlaySoundS2CPacket packet && packet.getCategory() == SoundCategory.BLOCKS
                && packet.getSound().value() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities())) {
                if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 144.0) {
                    mc.executeSync(() -> {
                        mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
                    });
                }
            }
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        if (!(event.getEntity() instanceof EndCrystalEntity crystalEntity)) {
            return;
        }
        Vec3d crystalPos = crystalEntity.getPos();
        BlockPos blockPos = BlockPos.ofFloored(crystalPos.add(0.0, -1.0, 0.0));
        boolean manualPos = manualCrystals.contains(blockPos);
        if (!instantConfig.getValue() && !(manualPos && manualConfig.getValue())) {
            return;
        }
        renderSpawnPos = blockPos;
        Long time = placePackets.remove(blockPos);
        attackRotate = time != null;
        if (attackRotate || manualPos) {
            attackInternal(crystalEntity, getCrystalHand());
            setStage("ATTACKING");
            lastAttackTimer.reset();
        } else if (instantCalcConfig.getValue()) {
            if (attackRangeCheck(crystalPos)) {
                return;
            }
            double selfDamage = EndCrystalUtil.getDamageTo(mc.player,
                    crystalPos, blockDestructionConfig.getValue());
            if (playerDamageCheck(selfDamage)) {
                return;
            }
            for (Entity entity : mc.world.getEntities()) {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || entity.getDisplayName() != null && Managers.SOCIAL.isFriend(entity.getDisplayName())) {
                    continue;
                }
                double crystalDist = crystalPos.squaredDistanceTo(entity.getPos());
                if (crystalDist > 144.0f) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue()) {
                    continue;
                }
                double damage = EndCrystalUtil.getDamageTo(entity,
                        crystalPos, blockDestructionConfig.getValue());
                // TODO: Test this
                DamageData<EndCrystalEntity> data = new DamageData<>(crystalEntity,
                        entity, damage, selfDamage, crystalEntity.getBlockPos().down());
                attackRotate = damage > instantDamageConfig.getValue() || attackCrystal != null
                        && damage >= attackCrystal.getDamage() && instantMaxConfig.getValue()
                        || entity instanceof LivingEntity entity1 && isCrystalLethalTo(data, entity1);
                if (attackRotate) {
                    attackInternal(crystalEntity, getCrystalHand());
                    setStage("ATTACKING");
                    lastAttackTimer.reset();
                    break;
                }
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket) {
            lastSwapTimer.reset();
        } else if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && !event.isClientPacket()
                && mc.player.getStackInHand(packet.getHand()).getItem() instanceof EndCrystalItem && manualConfig.getValue()) {
            BlockHitResult result = packet.getBlockHitResult();
            manualCrystals.add(result.getBlockPos());
        } else if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                && antiSurroundConfig.getValue() && canUseCrystalOnBlock(packet.getPos())) {
//            Vec3d crystalPos = crystalDamageVec(packet.getPos());
//            for (Entity entity : mc.world.getEntities()) {
//                if (entity == null || !entity.isAlive() || entity == mc.player
//                        || !isValidTarget(entity)
//                        || Managers.SOCIAL.isFriend(entity.getUuid())) {
//                    continue;
//                }
//                double crystalDist = crystalPos.squaredDistanceTo(entity.getPos());
//                if (crystalDist > 144.0f) {
//                    continue;
//                }
//                double dist = mc.player.squaredDistanceTo(entity);
//                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue()) {
//                    continue;
//                }
//                double damage = EndCrystalUtil.getDamageTo(entity,
//                        crystalPos, blockDestructionConfig.getValue());
//                if (!targetDamageCheck(damage, entity)) {
//                    placeCrystal(packet.getPos(), getCrystalHand());
//                    break;
//                }
//            }
        }
    }

    public boolean isAttacking() {
        return attackCrystal != null;
    }

    public boolean isPlacing() {
        return placeCrystal != null && isHoldingCrystal();
    }

    public void attackCrystal(EndCrystalEntity entity, Hand hand) {
        if (attackCheckPre(hand)) {
            return;
        }
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
        if (weakness != null && (strength == null || weakness.getAmplifier() > strength.getAmplifier())) {
            int slot = -1;
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem
                        || stack.getItem() instanceof AxeItem
                        || stack.getItem() instanceof PickaxeItem)) {
                    slot = i;
                    break;
                }
            }
            if (slot != -1) {
                boolean canSwap = antiWeaknessConfig.getValue() != Swap.NORMAL || autoSwapTimer.passed(500);
                if (antiWeaknessConfig.getValue() != Swap.OFF && canSwap) {
                    if (antiWeaknessConfig.getValue() == Swap.SILENT_ALT) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    } else if (antiWeaknessConfig.getValue() == Swap.SILENT) {
                        Managers.INVENTORY.setSlot(slot);
                    } else {
                        Managers.INVENTORY.setClientSlot(slot);
                    }
                }
                attackInternal(entity, Hand.MAIN_HAND);
                if (canSwap) {
                    if (antiWeaknessConfig.getValue() == Swap.SILENT_ALT) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    } else if (antiWeaknessConfig.getValue() == Swap.SILENT) {
                        Managers.INVENTORY.syncToClient();
                    }
                }
            }
        } else {
            attackInternal(entity, hand);
        }
    }

    private void attackInternal(EndCrystalEntity crystalEntity, Hand hand) {
        hand = hand != null ? hand : Hand.MAIN_HAND;
        // ((AccessorPlayerInteractEntityC2SPacket) packet).hookSetEntityId(id);
        Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
        attackPackets.put(crystalEntity.getId(), System.currentTimeMillis());
        if (swingConfig.getValue()) {
            mc.player.swingHand(hand);
        } else {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    private void placeCrystal(BlockPos blockPos, Hand hand) {
        if (checkMultitask()) {
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
        if (autoSwapConfig.getValue() != Swap.OFF && hand != Hand.OFF_HAND && getCrystalHand() == null) {
            if (isSilentSwap(autoSwapConfig.getValue()) && Managers.INVENTORY.count(Items.END_CRYSTAL) == 0) {
                return;
            }
            int crystalSlot = getCrystalSlot();
            if (crystalSlot != -1) {
                boolean canSwap = autoSwapConfig.getValue() != Swap.NORMAL || autoSwapTimer.passed(500);
                if (canSwap) {
                    if (autoSwapConfig.getValue() == Swap.SILENT_ALT) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    } else if (autoSwapConfig.getValue() == Swap.SILENT) {
                        Managers.INVENTORY.setSlot(crystalSlot);
                    } else {
                        Managers.INVENTORY.setClientSlot(crystalSlot);
                    }
                }
                placeInternal(result, Hand.MAIN_HAND);
                placePackets.put(blockPos, System.currentTimeMillis());
                if (canSwap) {
                    if (autoSwapConfig.getValue() == Swap.SILENT_ALT) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    } else if (autoSwapConfig.getValue() == Swap.SILENT) {
                        Managers.INVENTORY.syncToClient();
                    }
                }
            }
        } else if (isHoldingCrystal()) {
            placeInternal(result, hand);
            placePackets.put(blockPos, System.currentTimeMillis());
        }
    }

    private void placeInternal(BlockHitResult result, Hand hand) {
        if (hand == null) {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (swingConfig.getValue()) {
            mc.player.swingHand(hand);
        } else {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    private boolean isSilentSwap(Swap swap) {
        return swap == Swap.SILENT || swap == Swap.SILENT_ALT;
    }

    private int getCrystalSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof EndCrystalItem) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private Direction getPlaceDirection(BlockPos blockPos) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        if (strictDirectionConfig.getValue()) {
            if (mc.player.getY() >= blockPos.getY()) {
                return Direction.UP;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                Direction direction = result.getSide();
                if (!exposedDirectionConfig.getValue() || mc.world.isAir(blockPos.offset(direction))) {
                    return direction;
                }
            }
        } else {
            if (mc.world.isInBuildLimit(blockPos)) {
                return Direction.DOWN;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                    RaycastContext.ShapeType.OUTLINE,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                return result.getSide();
            }
        }
        return Direction.UP;
    }

    private DamageData<EndCrystalEntity> calculateAttackCrystal(List<Entity> entities) {
        if (entities.isEmpty()) {
            return null;
        }
        DamageData<EndCrystalEntity> data = null;
        for (Entity crystal : entities) {
            if (!(crystal instanceof EndCrystalEntity crystal1) || !crystal.isAlive()) {
                continue;
            }
            Long time = attackPackets.get(crystal.getId());
            boolean attacked = time != null && time < getBreakMs();
            if ((crystal.age < ticksExistedConfig.getValue() || attacked) && inhibitConfig.getValue()) {
                continue;
            }
            if (attackRangeCheck(crystal1)) {
                continue;
            }
            double selfDamage = EndCrystalUtil.getDamageTo(mc.player,
                    crystal.getPos(), blockDestructionConfig.getValue());
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.getValue()) {
                continue;
            }
            for (Entity entity : entities) {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || entity.getDisplayName() != null && Managers.SOCIAL.isFriend(entity.getDisplayName())) {
                    continue;
                }
                double crystalDist = crystal.squaredDistanceTo(entity);
                if (crystalDist > 144.0f) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue()) {
                    continue;
                }
                double damage = EndCrystalUtil.getDamageTo(entity,
                        crystal.getPos(), blockDestructionConfig.getValue());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity)) {
                    continue;
                }
                if (data == null || damage > data.getDamage()) {
                    data = new DamageData<>(crystal1, entity,
                            damage, selfDamage, crystal1.getBlockPos().down());
                }
            }
        }
        if (data == null || targetDamageCheck(data)) {
            return null;
        }
        return data;
    }

    private boolean attackRangeCheck(EndCrystalEntity entity) {
        return attackRangeCheck(entity.getPos());
    }

    /**
     * @param entityPos
     * @return
     */
    private boolean attackRangeCheck(Vec3d entityPos) {
        Vec3d playerPos = mc.player.getEyePos();
        double dist = playerPos.squaredDistanceTo(entityPos);
        if (dist > breakRangeConfig.getValue() * breakRangeConfig.getValue()) {
            return true;
        }
        double yOff = Math.abs(entityPos.getY() - mc.player.getY());
        if (yOff > maxYOffsetConfig.getValue()) {
            return true;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                playerPos, entityPos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() != HitResult.Type.MISS
                && dist > breakWallRangeConfig.getValue() * breakWallRangeConfig.getValue();
    }

    private DamageData<BlockPos> calculatePlaceCrystal(List<BlockPos> placeBlocks, List<Entity> entities) {
        if (placeBlocks.isEmpty() || entities.isEmpty()) {
            return null;
        }
        DamageData<BlockPos> data = null;
        for (BlockPos pos : placeBlocks) {
            if (!canUseCrystalOnBlock(pos) || placeRangeCheck(pos)) {
                continue;
            }
            double selfDamage = EndCrystalUtil.getDamageTo(mc.player,
                    crystalDamageVec(pos), blockDestructionConfig.getValue());
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.getValue()) {
                continue;
            }
            for (Entity entity : entities) {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || entity.getDisplayName() != null && Managers.SOCIAL.isFriend(entity.getDisplayName())) {
                    continue;
                }
                double blockDist = pos.getSquaredDistance(entity.getPos());
                if (blockDist > 144.0f) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue()) {
                    continue;
                }
                double damage = EndCrystalUtil.getDamageTo(entity,
                        crystalDamageVec(pos), blockDestructionConfig.getValue());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity)) {
                    continue;
                }
                if (data == null || damage > data.getDamage()) {
                    data = new DamageData<>(pos, entity, damage, selfDamage);
                }
            }
        }
        if (data == null || targetDamageCheck(data)) {
            return null;
        }
        return data;
    }

    /**
     * @param pos
     * @return
     */
    private boolean placeRangeCheck(BlockPos pos) {
        Vec3d player = placeRangeEyeConfig.getValue() ? mc.player.getEyePos() : mc.player.getPos();
        double dist = placeRangeCenterConfig.getValue() ?
                player.squaredDistanceTo(pos.toCenterPos()) : pos.getSquaredDistance(player.x, player.y, player.z);
        if (dist > placeRangeConfig.getValue() * placeRangeConfig.getValue()) {
            return true;
        }
        Vec3d raytrace = Vec3d.of(pos).add(0.0, raytraceConfig.getValue() ? 2.700000047683716 : 1.0, 0.0);
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                mc.player.getEyePos(), raytrace,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        float maxDist = breakRangeConfig.getValue() * breakRangeConfig.getValue();
        if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos() != pos) {
            maxDist = breakWallRangeConfig.getValue() * breakWallRangeConfig.getValue();
            if (dist > placeWallRangeConfig.getValue() * placeWallRangeConfig.getValue()) {
                return true;
            }
        }
        return breakValidConfig.getValue() && dist > maxDist;
    }

    private boolean checkOverrideSafety(boolean unsafeToPlayer, double damage, Entity entity) {
        return safetyOverride.getValue() && unsafeToPlayer && damage < EntityUtil.getHealth(entity) + 0.5;
    }

    private boolean targetDamageCheck(DamageData<?> crystal) {
        double minDmg = minDamageConfig.getValue();
        if (crystal.getAttackTarget() instanceof LivingEntity entity && isCrystalLethalTo(crystal, entity)) {
            minDmg = 2.0f;
        }
        return crystal.getDamage() < minDmg;
    }

    private boolean playerDamageCheck(double playerDamage) {
        if (!mc.player.isCreative()) {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (safetyConfig.getValue() && playerDamage >= health + 0.5f) {
                return true;
            }
            return playerDamage > maxLocalDamageConfig.getValue();
        }
        return false;
    }

    private boolean isFeetSurrounded(LivingEntity entity) {
        BlockPos pos1 = entity.getBlockPos();
        if (!mc.world.getBlockState(pos1).isReplaceable()) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }
            BlockPos pos2 = pos1.offset(direction);
            if (mc.world.getBlockState(pos2).isReplaceable()) {
                return false;
            }
        }
        return true;
    }

    private boolean isCrystalLethalTo(DamageData<?> crystal, LivingEntity entity) {
        if (!isFeetSurrounded(entity)) {
            return false;
        }
        if (lethalDamageConfig.getValue()) {
            return lastAttackTimer.passed(500);
        }
        float health = entity.getHealth() + entity.getAbsorptionAmount();
        if (crystal.getDamage() * (1.0f + lethalMultiplier.getValue()) >= health + 0.5f) {
            return true;
        }
        if (armorBreakerConfig.getValue()) {
            for (ItemStack armorStack : entity.getArmorItems()) {
                int n = armorStack.getDamage();
                int n1 = armorStack.getMaxDamage();
                float durability = ((n1 - n) / (float) n1) * 100.0f;
                if (durability < armorScaleConfig.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attackCheckPre(Hand hand) {
        if (!lastSwapTimer.passed(swapDelayConfig.getValue() * 25.0f)) {
            return true;
        }
        if (hand == Hand.MAIN_HAND) {
            return checkMultitask();
        }
        return false;
    }

    private boolean checkMultitask() {
        return !multitaskConfig.getValue() && mc.player.isUsingItem()
                || !whileMiningConfig.getValue() && mc.interactionManager.isBreakingBlock();
    }

    private boolean isHoldingCrystal() {
        if (!checkMultitask() && (autoSwapConfig.getValue() == Swap.SILENT || autoSwapConfig.getValue() == Swap.SILENT_ALT)) {
            return true;
        }
        return getCrystalHand() != null;
    }

    private Vec3d crystalDamageVec(BlockPos pos) {
        return Vec3d.of(pos).add(0.5, 1.0, 0.5);
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     */
    private boolean isValidTarget(Entity e) {
        return e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }

    /**
     * Returns <tt>true</tt> if an {@link EndCrystalItem} can be used on the
     * param {@link BlockPos}.
     *
     * @param p The block pos
     * @return Returns <tt>true</tt> if the crystal item can be placed on the
     * block
     */
    public boolean canUseCrystalOnBlock(BlockPos p) {
        BlockState state = mc.world.getBlockState(p);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) {
            return false;
        }
        BlockPos p2 = p.up();
        BlockState state2 = mc.world.getBlockState(p2);
        // ver 1.12.2 and below
        if (placementsConfig.getValue() == Placements.PROTOCOL && !mc.world.isAir(p2.up())) {
            return false;
        }
        if (!mc.world.isAir(p2) && !state2.isOf(Blocks.FIRE)) {
            return false;
        } else {
            final Box bb = Managers.NETWORK.isCrystalPvpCC() ? HALF_CRYSTAL_BB : FULL_CRYSTAL_BB;
            double d = p2.getX();
            double e = p2.getY();
            double f = p2.getZ();
            List<Entity> list = getEntitiesBlockingCrystal(new Box(d, e, f,
                    d + bb.maxX, e + bb.maxY, f + bb.maxZ));
            return list.isEmpty();
        }
    }

    private List<Entity> getEntitiesBlockingCrystal(Box box) {
        List<Entity> entities = new CopyOnWriteArrayList<>(
                mc.world.getOtherEntities(null, box));
        //
        for (Entity entity : entities) {
            if (entity == null || !entity.isAlive()
                    || entity instanceof ExperienceOrbEntity) {
                entities.remove(entity);
            } else if (entity instanceof EndCrystalEntity entity1
                    // && !intersectingCrystalCheck(entity1) // TODO: More advanced check for intersecting crystals
                    && entity1.getBoundingBox().intersects(box) || attackPackets.containsKey(entity.getId()) && entity.age < ticksExistedConfig.getValue()) {
                entities.remove(entity);
            }
        }
        return entities;
    }

    private boolean intersectingCrystalCheck(EndCrystalEntity entity) {
        // if (entity.age < ticksExistedConfig.getValue())
        // {
        //    return false;
        // }
        return attackRangeCheck(entity);
    }

    private List<BlockPos> getSphere(Vec3d origin) {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(placeRangeConfig.getValue());
        for (double x = -rad; x <= rad; ++x) {
            for (double y = -rad; y <= rad; ++y) {
                for (double z = -rad; z <= rad; ++z) {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    private boolean canHoldCrystal() {
        return isHoldingCrystal() || autoSwapConfig.getValue() != Swap.OFF && getCrystalSlot() != -1;
    }

    private Hand getCrystalHand() {
        final ItemStack offhand = mc.player.getOffHandStack();
        final ItemStack mainhand = mc.player.getMainHandStack();
        if (offhand.getItem() instanceof EndCrystalItem) {
            return Hand.OFF_HAND;
        } else if (mainhand.getItem() instanceof EndCrystalItem) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    // Debug info
    public void setStage(String crystalStage) {
        // this.crystalStage = crystalStage;
    }

    public int getBreakMs() {
        float avg = 0.0f;
        // fix ConcurrentModificationException
        ArrayList<Long> latencyCopy = Lists.newArrayList(attackLatency);
        if (!latencyCopy.isEmpty()) {
            for (float t : latencyCopy) {
                avg += t;
            }
            avg /= latencyCopy.size();
        }
        return (int) avg;
    }

    public enum Swap {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Sequential {
        NORMAL,
        STRICT,
        NONE
    }

    public enum Placements {
        NATIVE,
        PROTOCOL
    }

    public enum Rotate {
        FULL,
        SEMI,
        OFF
    }

    private static class DamageData<T> {
        //
        private final List<String> tags = new ArrayList<>();
        private T damageData;
        private Entity attackTarget;
        private BlockPos blockPos;
        //
        private double damage, selfDamage;

        //
        public DamageData() {

        }

        public DamageData(BlockPos damageData, Entity attackTarget, double damage, double selfDamage) {
            this.damageData = (T) damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = damageData;
        }

        public DamageData(T damageData, Entity attackTarget, double damage, double selfDamage, BlockPos blockPos) {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = blockPos;
        }

        //
        public void addTag(String tag) {
            tags.add(tag);
        }

        public void setDamageData(T damageData, Entity attackTarget, double damage, double selfDamage) {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
        }

        public T getDamageData() {
            return damageData;
        }

        public Entity getAttackTarget() {
            return attackTarget;
        }

        public double getDamage() {
            return damage;
        }

        public double getSelfDamage() {
            return selfDamage;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }
    }

    private class AttackCrystalTask implements Callable<DamageData<EndCrystalEntity>> {
        private final List<Entity> threadSafeEntities;

        public AttackCrystalTask(List<Entity> threadSafeEntities) {
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<EndCrystalEntity> call() throws Exception {
            return calculateAttackCrystal(threadSafeEntities);
        }
    }

    private class PlaceCrystalTask implements Callable<DamageData<BlockPos>> {
        private final List<BlockPos> threadSafeBlocks;
        private final List<Entity> threadSafeEntities;

        public PlaceCrystalTask(List<BlockPos> threadSafeBlocks,
                                List<Entity> threadSafeEntities) {
            this.threadSafeBlocks = threadSafeBlocks;
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<BlockPos> call() throws Exception {
            return calculatePlaceCrystal(threadSafeBlocks, threadSafeEntities);
        }
    }
}
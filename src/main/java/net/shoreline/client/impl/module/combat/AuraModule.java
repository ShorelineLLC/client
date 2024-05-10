package net.shoreline.client.impl.module.combat;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.impl.manager.world.tick.TickSync;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.client.util.world.EntityUtil;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author linus
 * @since 1.0
 */
public class AuraModule extends RotationModule {
    Config<Boolean> swingConfig = new BooleanConfig("Swing", "Swings the hand after attacking", true);
    Config<TargetMode> modeConfig = new EnumConfig<>("Mode", "The mode for targeting entities to attack", TargetMode.SWITCH, TargetMode.values());
    Config<Priority> priorityConfig = new EnumConfig<>("Priority", "The value to prioritize when searching for targets", Priority.HEALTH, Priority.values());
    Config<Float> searchRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f);
    Config<Float> rangeConfig = new NumberConfig<>("Range", "Range to attack entities", 1.0f, 4.5f, 6.0f);
    Config<Float> wallRangeConfig = new NumberConfig<>("WallRange", "Range to attack entities through walls", 1.0f, 4.5f, 6.0f);
    Config<Boolean> vanillaRangeConfig = new BooleanConfig("VanillaRange", "Only attack within vanilla range", false);
    Config<Float> fovConfig = new NumberConfig<>("FOV", "Field of view to attack entities", 1.0f, 180.0f, 180.0f);
    // Config<Boolean> latencyPositionConfig = new BooleanConfig("LatencyPosition", "Targets the latency positions of enemies", false);
    // Config<Integer> maxLatencyConfig = new NumberConfig<>("MaxLatency", "Maximum latency factor when calculating positions", 50, 250,1000, () -> latencyPositionConfig.getValue());
    Config<Boolean> attackDelayConfig = new BooleanConfig("AttackDelay", "Delays attacks according to minecraft hit delays for maximum damage per attack", true);
    Config<Float> attackSpeedConfig = new NumberConfig<>("AttackSpeed", "Delay for attacks (Only functions if AttackDelay is off)", 1.0f,20.0f, 20.0f, () -> !attackDelayConfig.getValue());
    Config<Float> randomSpeedConfig = new NumberConfig<>("RandomSpeed", "Randomized delay for attacks (Only functions if AttackDelay is off)", 0.0f, 0.0f, 10.0f,() -> !attackDelayConfig.getValue());
    // Config<Integer> packetsConfig = new NumberConfig<>("Packets", "Maximum attack packets to send in a single tick", 0, 1, 20);
    Config<Float> swapDelayConfig = new NumberConfig<>("SwapPenalty", "Delay for attacking after swapping items which prevents NCP flags", 0.0f,0.0f, 10.0f);
    Config<TickSync> tpsSyncConfig = new EnumConfig<>("TPS-Sync", "Syncs the attacks with the server TPS", TickSync.NONE, TickSync.values());
    Config<Boolean> awaitCritsConfig = new BooleanConfig("AwaitCriticals", "Aura will wait for a critical hit when falling", false);
    Config<Boolean> autoSwapConfig = new BooleanConfig("AutoSwap","Automatically swaps to a weapon before attacking", true);
    Config<Boolean> swordCheckConfig = new BooleanConfig("Sword-Check", "Checks if a weapon is in the hand before attacking", true);
    // ROTATE
    Config<Vector> hitVectorConfig = new EnumConfig<>("HitVector", "The vector to aim for when attacking entities", Vector.FEET, Vector.values());
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before attacking", false);
    Config<Boolean> silentRotateConfig = new BooleanConfig("RotateSilent", "Rotates silently to server", false, () -> rotateConfig.getValue());
    Config<Boolean> strictRotateConfig = new BooleanConfig("YawStep", "Rotates yaw over multiple ticks to prevent certain rotation flags in NCP", false, () -> rotateConfig.getValue());
    Config<Integer> rotateLimitConfig = new NumberConfig<>("YawStep-Limit", "Maximum yaw rotation in degrees for one tick", 1, 180, 180, NumberDisplay.DEGREES, () -> rotateConfig.getValue() && strictRotateConfig.getValue());
    Config<Integer> ticksExistedConfig = new NumberConfig<>("TicksExisted", "The minimum age of the entity to be considered for attack", 0, 50, 200);
    Config<Boolean> armorCheckConfig = new BooleanConfig("ArmorCheck", "Checks if target has armor before attacking", false);
    // Config<Boolean> autoBlockConfig = new BooleanConfig("AutoBlock", "Automatically blocks after attack", false);
    Config<Boolean> stopSprintConfig = new BooleanConfig("StopSprint", "Stops sprinting before attacking to maintain vanilla behavior", false);
    Config<Boolean> stopShieldConfig = new BooleanConfig("StopShield", "Automatically handles shielding before attacking", false);

    Config<Boolean> playersConfig = new BooleanConfig("Players", "Target players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Target monsters", false);
    Config<Boolean> neutralsConfig = new BooleanConfig("Neutrals", "Target neutrals", false);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Target animals", false);
    Config<Boolean> invisiblesConfig = new BooleanConfig("Invisibles", "Target invisible entities", true);
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders an indicator over the target", true);
    Config<Boolean> disableDeathConfig = new BooleanConfig("DisableOnDeath", "Disables during disconnect/death", false);

    private Entity entityTarget;
    private long randomDelay = -1;

    private boolean shielding;
    private boolean sneaking;
    private boolean sprinting;

    private final Timer attackTimer = new CacheTimer();
    private final Timer critTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    private final Timer switchTimer = new CacheTimer();
    private boolean rotated;

    private float[] silentRotations;

    public AuraModule() {
        super("Aura", "Attacks nearby entities", ModuleCategory.COMBAT, 700);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable() {
        entityTarget = null;
        silentRotations = null;
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        if (disableDeathConfig.getValue()) {
            disable();
        }
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (disableDeathConfig.getValue() && event.getEntity() == mc.player) {
            disable();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event) {
        if (Modules.AUTO_CRYSTAL.isAttacking()
                || Modules.AUTO_CRYSTAL.isPlacing()) {
            return;
        }
        final Vec3d eyepos = Managers.POSITION.getEyePos();
        switch (modeConfig.getValue()) {
            case SWITCH -> entityTarget = getAttackTarget(eyepos);
            case SINGLE -> {
                if (entityTarget == null || !entityTarget.isAlive()
                        || !isInAttackRange(eyepos, entityTarget)) {
                    entityTarget = getAttackTarget(eyepos);
                }
            }
        }
        if (entityTarget == null || !switchTimer.passed(swapDelayConfig.getValue() * 25.0f)) {
            silentRotations = null;
            return;
        }
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
                || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed()) {
            autoSwapTimer.reset();
        }
        // END PRE
        boolean sword = mc.player.getMainHandStack().getItem() instanceof SwordItem;
        if (autoSwapConfig.getValue() && autoSwapTimer.passed(500) && !sword) {
            int slot = getSwordSlot();
            if (slot != -1) {
                Managers.INVENTORY.setClientSlot(slot);
            }
        }
        if (!isHoldingSword()) {
            return;
        }
        if (rotateConfig.getValue()) {
            float[] rotation = RotationUtil.getRotationsTo(mc.player.getEyePos(),
                    getAttackRotateVec(entityTarget));
            if (!silentRotateConfig.getValue() && strictRotateConfig.getValue()) {
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotation[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f) {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimitConfig.getValue();
                float yaw;
                if (diff1 > rotateLimitConfig.getValue()) {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                } else {
                    yaw = rotation[0];
                    rotated = true;
                }
                rotation[0] = yaw;
            } else {
                rotated = true;
            }
            // what what you cannot hop in my car
            // bentley coupe ridin with stars
            if (silentRotateConfig.getValue())
            {
                silentRotations = rotation;
            }
            else
            {
                setRotation(rotation[0], rotation[1]);
            }
        }
        if (isRotationBlocked() || !shouldWaitCrit() || !rotated && rotateConfig.getValue() || !isInAttackRange(eyepos, entityTarget)) {
            return;
        }
        if (attackDelayConfig.getValue()) {
            float ticks = 20.0f - Managers.TICK.getTickSync(tpsSyncConfig.getValue());
            float progress = mc.player.getAttackCooldownProgress(ticks);
            if (progress >= 1.0f && attackTarget(entityTarget)) {
                mc.player.resetLastAttackedTicks();
            }
        } else {
            if (randomDelay < 0) {
                randomDelay = (long) RANDOM.nextFloat((randomSpeedConfig.getValue() * 10.0f) + 1.0f);
            }
            float delay = (attackSpeedConfig.getValue() * 50.0f) + randomDelay;
            if (attackTimer.passed(1000.0f - delay) && attackTarget(entityTarget)) {
                randomDelay = -1;
                attackTimer.reset();
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer.reset();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (entityTarget != null && renderConfig.getValue() && isHoldingSword()) {
            int attackDelay;
            float delay = (attackSpeedConfig.getValue() * 50.0f) + randomDelay;
            if (attackDelayConfig.getValue()) {
                float animFactor = 1.0f - mc.player.getAttackCooldownProgress(0.0f);
                attackDelay = (int) (100.0 * animFactor);
            }
            else {
                float animFactor = 1.0f - MathHelper.clamp(attackTimer.getElapsedTime() / (1000f - delay), 0.0f, 1.0f);
                attackDelay = (int) (100.0 * animFactor);
            }
            RenderManager.renderBox(event.getMatrices(),
                    Interpolation.getInterpolatedEntityBox(entityTarget), Modules.COLORS.getRGB(60 + attackDelay));
            RenderManager.renderBoundingBox(event.getMatrices(),
                    Interpolation.getInterpolatedEntityBox(entityTarget), 1.5f, Modules.COLORS.getRGB(145));
        }
    }

    private boolean attackTarget(Entity entity) {
/*
        Entity castEntity;
        // validate our server-sided rotations
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return false;
        }
        // Get the entity raycasted & then check. If invalid, fail
        castEntity = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (castEntity == null || !castEntity.isAttackable()) {
            return false;
        }
        preAttackTarget();
        mc.doAttack();
        postAttackTarget(castEntity);
*/
        preAttackTarget();

        if (silentRotateConfig.getValue() && silentRotations != null)
        {
            setRotationSilent(silentRotations[0], silentRotations[1]);
        }

        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity,
                Managers.POSITION.isSneaking());
        Managers.NETWORK.sendPacket(packet);
        postAttackTarget(entity);
        if (swingConfig.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        if (silentRotateConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync(true);
        }
        return true;
    }

    private int getSwordSlot() {
        float sharp = 0.0f;
        int slot = -1;
        // Maximize item attack damage
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem swordItem) {
                float sharpness = EnchantmentHelper.getLevel(
                        Enchantments.SHARPNESS, stack) * 0.5f + 0.5f;
                float dmg = swordItem.getAttackDamage() + sharpness;
                if (dmg > sharp) {
                    sharp = dmg;
                    slot = i;
                }
            } else if (stack.getItem() instanceof AxeItem axeItem) {
                float sharpness = EnchantmentHelper.getLevel(
                        Enchantments.SHARPNESS, stack) * 0.5f + 0.5f;
                float dmg = axeItem.getAttackDamage() + sharpness;
                if (dmg > sharp) {
                    sharp = dmg;
                    slot = i;
                }
            } else if (stack.getItem() instanceof TridentItem) {
                float sharpness = EnchantmentHelper.getLevel(
                        Enchantments.SHARPNESS, stack) * 0.5f + 0.5f;
                float dmg = TridentItem.ATTACK_DAMAGE + sharpness;
                if (dmg > sharp) {
                    sharp = dmg;
                    slot = i;
                }
            }
        }
        return slot;
    }

    private void preAttackTarget() {
        final ItemStack offhand = mc.player.getOffHandStack();
        // Shield state
        shielding = false;
        if (stopShieldConfig.getValue()) {
            shielding = offhand.getItem() == Items.SHIELD && mc.player.isBlocking();
            if (shielding) {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        Managers.POSITION.getBlockPos(), Direction.getFacing(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ())));
            }
        }
        sneaking = false;
        sprinting = false;
        if (stopSprintConfig.getValue()) {
            sneaking = Managers.POSITION.isSneaking();
            if (sneaking) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            sprinting = Managers.POSITION.isSprinting();
            if (sprinting) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    // RELEASE
    private void postAttackTarget(Entity entity) {
        if (shielding) {
            Managers.NETWORK.sendSequencedPacket(s ->
                    new PlayerInteractItemC2SPacket(Hand.OFF_HAND, s));
        }
        if (sneaking) {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        if (sprinting) {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
        if (Modules.CRITICALS.isEnabled() && critTimer.passed(500)) {
            if (!mc.player.isOnGround()
                    || mc.player.isRiding()
                    || mc.player.isSubmergedInWater()
                    || mc.player.isInLava()
                    || mc.player.isHoldingOntoLadder()
                    || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                    || mc.player.input.jumping) {
                return;
            }
            Modules.CRITICALS.preAttackPacket();
            critTimer.reset();
            mc.player.addCritParticles(entity);
        }
    }

    private Entity getAttackTarget(Vec3d pos) {
        double min = Double.MAX_VALUE;
        Entity attackTarget = null;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == null || entity == mc.player
                    || !entity.isAlive() || !isEnemy(entity)
                    || Managers.SOCIAL.isFriend(entity.getName())
                    || entity instanceof EndCrystalEntity
                    || entity instanceof ItemEntity
                    || entity instanceof ArrowEntity
                    || entity instanceof ExperienceBottleEntity
                    || entity instanceof PlayerEntity player && player.isCreative()) {
                continue;
            }
            if (armorCheckConfig.getValue()
                    && entity instanceof LivingEntity
                    && !entity.getArmorItems().iterator().hasNext()) {
                continue;
            }
            double dist = pos.distanceTo(entity.getPos());
            if (dist <= searchRangeConfig.getValue()) {
                if (entity.age < ticksExistedConfig.getValue()) {
                    continue;
                }
                switch (priorityConfig.getValue()) {
                    case DISTANCE -> {
                        if (dist < min) {
                            min = dist;
                            attackTarget = entity;
                        }
                    }
                    case HEALTH -> {
                        if (entity instanceof LivingEntity e) {
                            float health = e.getHealth() + e.getAbsorptionAmount();
                            if (health < min) {
                                min = health;
                                attackTarget = entity;
                            }
                        }
                    }
                    case ARMOR -> {
                        if (entity instanceof LivingEntity e) {
                            float armor = getArmorDurability(e);
                            if (armor < min) {
                                min = armor;
                                attackTarget = entity;
                            }
                        }
                    }
                }
            }
        }
        return attackTarget;
    }

    private float getArmorDurability(LivingEntity e) {
        float edmg = 0.0f;
        float emax = 0.0f;
        for (ItemStack armor : e.getArmorItems()) {
            if (armor != null && !armor.isEmpty()) {
                edmg += armor.getDamage();
                emax += armor.getMaxDamage();
            }
        }
        return 100.0f - edmg / emax;
    }

    public boolean isInAttackRange(Vec3d pos, Entity entity) {
        final Vec3d entityPos = getAttackRotateVec(entity);
        double dist = pos.distanceTo(entityPos);
        return isInAttackRange(dist, pos, entityPos);
    }

    /**
     * @param dist
     * @param pos
     * @return
     */
    public boolean isInAttackRange(double dist, Vec3d pos, Vec3d entityPos) {
        if (vanillaRangeConfig.getValue() && dist > 3.0f) {
            return false;
        }
        if (dist > rangeConfig.getValue()) {
            return false;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                pos, entityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        if (result != null && dist > wallRangeConfig.getValue()) {
            return false;
        }
        if (fovConfig.getValue() != 180.0f) {
            float[] rots = RotationUtil.getRotationsTo(pos, entityPos);
            float diff = MathHelper.wrapDegrees(mc.player.getYaw()) - rots[0];
            float magnitude = Math.abs(diff);
            return magnitude <= fovConfig.getValue();
        }
        return true;
    }

    public boolean isHoldingSword() {
        return !swordCheckConfig.getValue() || mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof AxeItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem;
    }

    public boolean shouldWaitCrit() {
        return !mc.player.isOnGround()
                && mc.player.fallDistance > 0
                && mc.player.fallDistance < 1
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !mc.player.isClimbing()
                && !mc.player.isTouchingWater()
                || !awaitCritsConfig.getValue()
                || !mc.options.jumpKey.isPressed();
    }

    private Vec3d getAttackRotateVec(Entity entity) {
        Vec3d feetPos = entity.getPos();
        return switch (hitVectorConfig.getValue()) {
            case FEET -> feetPos;
            case TORSO -> feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
            case EYES -> entity.getEyePos();
            case AUTO -> {
                Vec3d torsoPos = feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
                Vec3d eyesPos = entity.getEyePos();
                yield Stream.of(feetPos, torsoPos, eyesPos).min(Comparator.comparing(b -> mc.player.getEyePos().squaredDistanceTo(b))).orElse(eyesPos);
            }
        };
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     * @see EntityUtil
     */
    private boolean isEnemy(Entity e) {
        return (!e.isInvisible() || invisiblesConfig.getValue())
                && e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }

    public Entity getEntityTarget() {
        return entityTarget;
    }

    public enum TargetMode {
        SWITCH,
        SINGLE
    }

    public enum Vector {
        EYES,
        TORSO,
        FEET,
        AUTO
    }

    public enum Priority {
        HEALTH,
        DISTANCE,
        ARMOR
    }
}
package net.shoreline.client.impl.module.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.impl.manager.world.tick.TickSync;
import net.shoreline.client.impl.module.CombatModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.world.AutoMineModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.EnchantmentUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author linus
 * @since 1.0
 */
public class AuraModule extends CombatModule
{
    private static AuraModule INSTANCE;

    Config<Boolean> swingConfig = register(new BooleanConfig("Swing", "Swings the hand after attacking", true));
    Config<TargetMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for targeting entities to attack", TargetMode.SWITCH, TargetMode.values()));
    Config<Priority> priorityConfig = register(new EnumConfig<>("Priority", "The value to prioritize when searching for targets", Priority.HEALTH, Priority.values()));
    Config<Float> searchRangeConfig = register(new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "Range to attack entities", 1.0f, 4.5f, 6.0f));
    Config<Float> wallRangeConfig = register(new NumberConfig<>("WallRange", "Range to attack entities through walls", 1.0f, 4.5f, 6.0f));
    Config<Boolean> vanillaRangeConfig = register(new BooleanConfig("VanillaRange", "Only attack within vanilla range", false));
    Config<Float> fovConfig = register(new NumberConfig<>("FOV", "Field of view to attack entities", 1.0f, 180.0f, 180.0f));
    // Config<Boolean> latencyPositionConfig = register(new BooleanConfig("LatencyPosition", "Targets the latency positions of enemies", false);
    // Config<Integer> maxLatencyConfig = register(new NumberConfig<>("MaxLatency", "Maximum latency factor when calculating positions", 50, 250,1000, () -> latencyPositionConfig.getValue());
    Config<Boolean> attackDelayConfig = register(new BooleanConfig("AttackDelay", "Delays attacks according to minecraft hit delays for maximum damage per attack", true));
    Config<Float> attackSpeedConfig = register(new NumberConfig<>("AttackSpeed", "Delay for attacks (Only functions if AttackDelay is off)", 1.0f, 20.0f, 20.0f, () -> !attackDelayConfig.getValue()));
    Config<Float> randomSpeedConfig = register(new NumberConfig<>("RandomSpeed", "Randomized delay for attacks (Only functions if AttackDelay is off)", 0.0f, 0.0f, 10.0f, () -> !attackDelayConfig.getValue()));
    // Config<Integer> packetsConfig = register(new NumberConfig<>("Packets", "Maximum attack packets to send in a single tick", 0, 1, 20);
    Config<Float> swapDelayConfig = register(new NumberConfig<>("SwapPenalty", "Delay for attacking after swapping items which prevents NCP flags", 0.0f, 0.0f, 10.0f));
    Config<TickSync> tpsSyncConfig = register(new EnumConfig<>("TPS-Sync", "Syncs the attacks with the server TPS", TickSync.NONE, TickSync.values()));
    Config<Swap> autoSwapConfig = register(new EnumConfig<>("AutoSwap", "Automatically swaps to a weapon before attacking", Swap.OFF, Swap.values()));
    Config<Boolean> swordCheckConfig = register(new BooleanConfig("Sword-Check", "Checks if a weapon is in the hand before attacking", true));
    // ROTATE
    Config<Vector> hitVectorConfig = register(new EnumConfig<>("HitVector", "The vector to aim for when attacking entities", Vector.FEET, Vector.values()));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotate before attacking", false));
    Config<Boolean> silentRotateConfig = register(new BooleanConfig("RotateSilent", "Rotates silently to server", false, () -> rotateConfig.getValue()));
    Config<Boolean> strictRotateConfig = register(new BooleanConfig("YawStep", "Rotates yaw over multiple ticks to prevent certain rotation flags in NCP", false, () -> rotateConfig.getValue()));
    Config<Integer> rotateLimitConfig = register(new NumberConfig<>("YawStep-Limit", "Maximum yaw rotation in degrees for one tick", 1, 180, 180, NumberDisplay.DEGREES, () -> rotateConfig.getValue() && strictRotateConfig.getValue()));
    Config<Integer> ticksExistedConfig = register(new NumberConfig<>("TicksExisted", "The minimum age of the entity to be considered for attack", 0, 0, 200));
    Config<Boolean> armorCheckConfig = register(new BooleanConfig("ArmorCheck", "Checks if target has armor before attacking", false));
    // Config<Boolean> autoBlockConfig = register(new BooleanConfig("AutoBlock", "Automatically blocks after attack", false);
    Config<Boolean> stopSprintConfig = register(new BooleanConfig("StopSprint", "Stops sprinting before attacking to maintain vanilla behavior", false));
    Config<Boolean> stopShieldConfig = register(new BooleanConfig("StopShield", "Automatically handles shielding before attacking", false));
    Config<Boolean> maceBreachConfig = register(new BooleanConfig("MaceBreach", "Abuses vanilla exploit to apply breach enchantment to swords", false, () -> autoSwapConfig.getValue() != Swap.SILENT));

    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Target players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Target monsters", false));
    Config<Boolean> neutralsConfig = register(new BooleanConfig("Neutrals", "Target neutrals", false));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Target animals", false));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Target invisible entities", true));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders an indicator over the target", true));
    Config<Boolean> disableDeathConfig = register(new BooleanConfig("DisableOnDeath", "Disables during disconnect/death", false));

    private Entity entityTarget;
    private long randomDelay = -1;

    private boolean shielding;
    private boolean sneaking;
    private boolean sprinting;

    private long lastAttackTime;
    private final Timer critTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    private final Timer switchTimer = new CacheTimer();
    private boolean rotated;

    private float[] silentRotations;

    public AuraModule()
    {
        super("Aura", "Attacks nearby entities", ModuleCategory.COMBAT, 700);
        INSTANCE = this;
    }

    public static AuraModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable()
    {
        entityTarget = null;
        silentRotations = null;
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        if (disableDeathConfig.getValue())
        {
            disable();
        }
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (disableDeathConfig.getValue() && event.getEntity() == mc.player)
        {
            disable();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        if (AutoCrystalModule.getInstance().isAttacking()
                || AutoCrystalModule.getInstance().isPlacing()
                || autoSwapConfig.getValue() == Swap.SILENT && AutoMineModule.getInstance().isSilentSwapping()
                || mc.player.isSpectator())
        {
            return;
        }

        if (!multitaskConfig.getValue() && checkMultitask(true))
        {
            return;
        }

        final Vec3d eyepos = Managers.POSITION.getEyePos();
        entityTarget = switch (modeConfig.getValue())
        {
            case SWITCH -> getAttackTarget(eyepos);
            case SINGLE ->
            {
                if (entityTarget == null || !entityTarget.isAlive()
                        || !isInAttackRange(eyepos, entityTarget))
                {
                    yield getAttackTarget(eyepos);
                }
                yield entityTarget;
            }
        };
        if (entityTarget == null || !switchTimer.passed(swapDelayConfig.getValue() * 25.0f))
        {
            silentRotations = null;
            return;
        }
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
                || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed())
        {
            autoSwapTimer.reset();
        }

        int slot = getSwordSlot();
        // END PRE
        boolean silentSwapped = false;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) && slot != -1)
        {
            switch (autoSwapConfig.getValue())
            {
                case NORMAL ->
                {
                    if (autoSwapTimer.passed(500))
                    {
                        Managers.INVENTORY.setClientSlot(slot);
                    }
                }
                case SILENT ->
                {
                    Managers.INVENTORY.setSlot(slot);
                    silentSwapped = true;
                }
            }
        }
        if (!isHoldingSword() && autoSwapConfig.getValue() != Swap.SILENT)
        {
            return;
        }
        if (rotateConfig.getValue())
        {
            float[] rotation = RotationUtil.getRotationsTo(mc.player.getEyePos(),
                    getAttackRotateVec(entityTarget));
            if (!silentRotateConfig.getValue() && strictRotateConfig.getValue())
            {
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotation[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f)
                {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimitConfig.getValue();
                float yaw;
                if (diff1 > rotateLimitConfig.getValue())
                {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                }
                else
                {
                    yaw = rotation[0];
                    rotated = true;
                }
                rotation[0] = yaw;
            }
            else
            {
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
        if (isRotationBlocked() || !rotated && rotateConfig.getValue() || !isInAttackRange(eyepos, entityTarget))
        {
            Managers.INVENTORY.syncToClient();
            return;
        }
        if (attackDelayConfig.getValue())
        {
            PlayerInventory inventory = mc.player.getInventory();
            ItemStack itemStack = inventory.getStack((slot == -1 || !swordCheckConfig.getValue()) ? mc.player.getInventory().selectedSlot : slot);

            MutableDouble attackSpeed = new MutableDouble(
                    mc.player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));

            AttributeModifiersComponent attributeModifiers =
                    itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (attributeModifiers != null)
            {
                attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) ->
                {
                    if (entry == EntityAttributes.GENERIC_ATTACK_SPEED)
                    {
                        attackSpeed.add(modifier.value());
                    }
                });
            }

            double attackCooldownTicks = 1.0 / attackSpeed.getValue() * 20.0;

            int breachSlot = getBreachMaceSlot();
            if (autoSwapConfig.getValue() != Swap.SILENT && maceBreachConfig.getValue() && breachSlot != -1)
            {
                Managers.INVENTORY.setSlot(breachSlot);
            }

            float ticks = 20.0f - Managers.TICK.getTickSync(tpsSyncConfig.getValue());
            float currentTime = (System.currentTimeMillis() - lastAttackTime) + (ticks * 50.0f);
            if ((currentTime / 50.0f) >= attackCooldownTicks && attackTarget(entityTarget))
            {
                lastAttackTime = System.currentTimeMillis();
            }

            if (autoSwapConfig.getValue() != Swap.SILENT && maceBreachConfig.getValue() && breachSlot != -1)
            {
                Managers.INVENTORY.syncToClient();
            }
        }
        else
        {
            if (randomDelay < 0)
            {
                randomDelay = (long) RANDOM.nextFloat((randomSpeedConfig.getValue() * 10.0f) + 1.0f);
            }
            float delay = (attackSpeedConfig.getValue() * 50.0f) + randomDelay;

            int breachSlot = getBreachMaceSlot();
            if (autoSwapConfig.getValue() != Swap.SILENT && maceBreachConfig.getValue() && breachSlot != -1)
            {
                Managers.INVENTORY.setSlot(breachSlot);
            }

            long currentTime = System.currentTimeMillis() - lastAttackTime;
            if (currentTime >= 1000.0f - delay && attackTarget(entityTarget))
            {
                randomDelay = -1;
                lastAttackTime = System.currentTimeMillis();
            }

            if (autoSwapConfig.getValue() != Swap.SILENT && maceBreachConfig.getValue() && breachSlot != -1)
            {
                Managers.INVENTORY.syncToClient();
            }
        }

        if (autoSwapConfig.getValue() == Swap.SILENT && silentSwapped)
        {
            Managers.INVENTORY.syncToClient();
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
            switchTimer.reset();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (AutoCrystalModule.getInstance().isAttacking()
                || AutoCrystalModule.getInstance().isPlacing() || mc.player.isSpectator())
        {
            return;
        }
        if (entityTarget != null && renderConfig.getValue() && (isHoldingSword() || autoSwapConfig.getValue() == Swap.SILENT))
        {
            long currentTime = System.currentTimeMillis() - lastAttackTime;
            float animFactor = 1.0f - MathHelper.clamp(currentTime / 1000f, 0.0f, 1.0f);
            int attackDelay = (int) (70.0 * animFactor);
            RenderBuffers.preRender();
            RenderManager.renderBox(event.getMatrices(),
                    Interpolation.getInterpolatedEntityBox(entityTarget), ColorsModule.getInstance().getRGB(30 + attackDelay));
            RenderManager.renderBoundingBox(event.getMatrices(),
                    Interpolation.getInterpolatedEntityBox(entityTarget), 1.5f, ColorsModule.getInstance().getRGB(100));
            RenderBuffers.postRender();
        }
    }

    private boolean attackTarget(Entity entity)
    {
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

        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking());
        Managers.NETWORK.sendPacket(packet);
        if (swingConfig.getValue())
        {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        else
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        postAttackTarget(entity);

        if (silentRotateConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
        return true;
    }

    private int getSwordSlot()
    {
        float sharp = 0.0f;
        int slot = -1;
        // Maximize item attack damage
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem swordItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = swordItem.getMaterial().getAttackDamage() + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof AxeItem axeItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = axeItem.getMaterial().getAttackDamage() + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof TridentItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = TridentItem.ATTACK_DAMAGE + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof MaceItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                        Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = 5.0f + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
        }
        return slot;
    }

    private int getBreachMaceSlot()
    {
        int slot = -1;
        int maxBreach = 0;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof MaceItem))
            {
                continue;
            }
            int breach = EnchantmentUtil.getLevel(stack, Enchantments.BREACH);
            if (breach > maxBreach)
            {
                slot = i;
                maxBreach = breach;
            }
        }
        return slot;
    }

    private void preAttackTarget()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        // Shield state
        shielding = false;
        if (stopShieldConfig.getValue())
        {
            shielding = offhand.getItem() == Items.SHIELD && mc.player.isBlocking();
            if (shielding)
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        Managers.POSITION.getBlockPos(), Direction.getFacing(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ())));
            }
        }
        sneaking = false;
        sprinting = false;
        if (stopSprintConfig.getValue())
        {
            sneaking = Managers.POSITION.isSneaking();
            if (sneaking)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            sprinting = Managers.POSITION.isSprinting();
            if (sprinting)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    // RELEASE
    private void postAttackTarget(Entity entity)
    {
        if (shielding)
        {
            Managers.NETWORK.sendSequencedPacket(s ->
                    new PlayerInteractItemC2SPacket(Hand.OFF_HAND, s, mc.player.getYaw(), mc.player.getPitch()));
        }
        if (sneaking)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        if (sprinting)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
//        if (CriticalsModule.getInstance().isEnabled() && critTimer.passed(500)) {
//            if (!mc.player.isOnGround()
//                    || mc.player.isRiding()
//                    || mc.player.isSubmergedInWater()
//                    || mc.player.isInLava()
//                    || mc.player.isHoldingOntoLadder()
//                    || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
//                    || mc.player.input.jumping) {
//                return;
//            }
//            CriticalsModule.getInstance().preAttackPacket(entity);
//            critTimer.reset();
//        }
    }

    private Entity getAttackTarget(Vec3d pos)
    {
        double min = Double.MAX_VALUE;
        Entity attackTarget = null;
        for (Entity entity : mc.world.getEntities())
        {
            if (entity == null || entity == mc.player
                    || !entity.isAlive() || !isEnemy(entity)
                    || Managers.SOCIAL.isFriend(entity.getName())
                    || entity instanceof EndCrystalEntity
                    || entity instanceof ItemEntity
                    || entity instanceof ArrowEntity
                    || entity instanceof ExperienceBottleEntity)
            {
                continue;
            }
            if (armorCheckConfig.getValue()
                    && entity instanceof LivingEntity livingEntity
                    && !livingEntity.getArmorItems().iterator().hasNext())
            {
                continue;
            }
            double dist = pos.distanceTo(entity.getPos());
            if (dist <= searchRangeConfig.getValue())
            {
                if (entity.age < ticksExistedConfig.getValue())
                {
                    continue;
                }
                switch (priorityConfig.getValue())
                {
                    case DISTANCE ->
                    {
                        if (dist < min)
                        {
                            min = dist;
                            attackTarget = entity;
                        }
                    }
                    case HEALTH ->
                    {
                        if (entity instanceof LivingEntity e)
                        {
                            float health = e.getHealth() + e.getAbsorptionAmount();
                            if (health < min)
                            {
                                min = health;
                                attackTarget = entity;
                            }
                        }
                    }
                    case ARMOR ->
                    {
                        if (entity instanceof LivingEntity e)
                        {
                            float armor = getArmorDurability(e);
                            if (armor < min)
                            {
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

    private float getArmorDurability(LivingEntity e)
    {
        float edmg = 0.0f;
        float emax = 0.0f;
        for (ItemStack armor : e.getArmorItems())
        {
            if (armor != null && !armor.isEmpty())
            {
                edmg += armor.getDamage();
                emax += armor.getMaxDamage();
            }
        }
        return 100.0f - edmg / emax;
    }

    public boolean isInAttackRange(Vec3d pos, Entity entity)
    {
        final Vec3d entityPos = getAttackRotateVec(entity);
        double dist = pos.distanceTo(entityPos);
        return isInAttackRange(dist, pos, entityPos);
    }

    /**
     * @param dist
     * @param pos
     * @return
     */
    public boolean isInAttackRange(double dist, Vec3d pos, Vec3d entityPos)
    {
        if (vanillaRangeConfig.getValue() && dist > 3.0f)
        {
            return false;
        }
        if (dist > rangeConfig.getValue())
        {
            return false;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
                pos, entityPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        if (result != null && !result.getBlockPos().equals(BlockPos.ofFloored(entityPos)) && dist > wallRangeConfig.getValue())
        {
            return false;
        }
        if (fovConfig.getValue() != 180.0f)
        {
            float[] rots = RotationUtil.getRotationsTo(pos, entityPos);
            float diff = MathHelper.wrapDegrees(mc.player.getYaw()) - rots[0];
            float magnitude = Math.abs(diff);
            return magnitude <= fovConfig.getValue();
        }
        return true;
    }

    public boolean isHoldingSword()
    {
        return !swordCheckConfig.getValue() || mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof AxeItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem
                || mc.player.getMainHandStack().getItem() instanceof MaceItem;
    }

    private Vec3d getAttackRotateVec(Entity entity)
    {
        Vec3d feetPos = entity.getPos();
        return switch (hitVectorConfig.getValue())
        {
            case FEET -> feetPos;
            case TORSO -> feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
            case EYES -> entity.getEyePos();
            case AUTO ->
            {
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
    private boolean isEnemy(Entity e)
    {
        return (!e.isInvisible() || invisiblesConfig.getValue())
                && e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }

    public Entity getEntityTarget()
    {
        return entityTarget;
    }

    public enum TargetMode
    {
        SWITCH,
        SINGLE
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        OFF
    }

    public enum Vector
    {
        EYES,
        TORSO,
        FEET,
        AUTO
    }

    public enum Priority
    {
        HEALTH,
        DISTANCE,
        ARMOR
    }
}
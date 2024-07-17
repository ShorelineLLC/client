package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.EntityTravelEvent;
import net.shoreline.client.impl.event.entity.FallFlyingEvent;
import net.shoreline.client.impl.event.entity.JumpDelayEvent;
import net.shoreline.client.impl.event.entity.player.PlayerMoveEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.module.exploit.ExtendedFireworkModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class ElytraFlyModule extends RotationModule
{

    Config<FlyMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for elytra flight", FlyMode.CONTROL, FlyMode.values()));
    Config<Float> pitchConfig = register(new NumberConfig<>("Pitch", "The pitch for bounce recast", 0.0f, 75.0f, 90.0f, () -> modeConfig.getValue() == FlyMode.BOUNCE));
    Config<Boolean> boostConfig = register(new BooleanConfig("Boost", "Applies boost to bounce motion", false, () -> modeConfig.getValue() == FlyMode.BOUNCE));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f, () -> modeConfig.getValue() != FlyMode.BOUNCE || boostConfig.getValue()));
    Config<Float> vspeedConfig = register(new NumberConfig<>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f, () -> modeConfig.getValue() != FlyMode.BOUNCE && modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.FACTORIZE));
    Config<Boolean> accelerationConfig = register(new BooleanConfig("Acceleration", "Accelerates fly speed", false, () -> modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Float> accelSpeedConfig = register(new NumberConfig<>("AccelSpeed", "Acceleration speed", 0.01f, 0.21f, 1.00f, () -> accelerationConfig.getValue() && modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Float> maxSpeedConfig = register(new NumberConfig<>("MaxSpeed", "The maximum flight speed", 0.1f, 3.5f, 10.0f, () -> accelerationConfig.getValue() && modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE || modeConfig.getValue() == FlyMode.BOOST || (modeConfig.getValue() == FlyMode.BOUNCE && boostConfig.getValue())));
    Config<Boolean> instantFlyConfig = register(new BooleanConfig("InstantFly", "Automatically activates elytra from the ground", false, () -> modeConfig.getValue() != FlyMode.BOUNCE && modeConfig.getValue() != FlyMode.PACKET));
    Config<Boolean> infiniteDurabilityConfig = register(new BooleanConfig("InfiniteDurability", "Prevents elytra from using durability", false, () -> modeConfig.getValue() == FlyMode.PACKET));
    Config<Boolean> lagRedeployConfig = register(new BooleanConfig("LagRedeploy", "Redeploys elytra when lagging", false, () -> modeConfig.getValue() == FlyMode.CONTROL));
    Config<Boolean> fireworkConfig = register(new BooleanConfig("Fireworks", "Uses fireworks when flying", false, () -> modeConfig.getValue() == FlyMode.CONTROL));
    Config<Boolean> baritoneConfig = new BooleanConfig("Baritone", "Uses baritone to automatically navigate around obstacles", false);

    private float speed;
    private float pitch;
    private float prevPitch;
    private final Timer lagTimer = new CacheTimer();
    private final Timer redeployTimer = new CacheTimer();
    private final Timer takeoffTimer = new CacheTimer();
    private final Timer bounceTimer = new CacheTimer();
    private int fireworkLifetime;
    private boolean previousFallFlying;

    public ElytraFlyModule()
    {
        super("ElytraFly", "Allows you to fly freely using an elytra", ModuleCategory.MOVEMENT);
        if (ShorelineMod.isBaritonePresent())
        {
            register(baritoneConfig);
        }
    }

    @Override
    public String getModuleData()
    {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onEnable()
    {
        speed = 0.0f;
    }

    @Override
    public void onDisable()
    {
        Managers.TICK.setClientTick(1.0f);
        mc.options.jumpKey.setPressed(false);
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05f);
        fireworkLifetime = 0;
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        fireworkLifetime--;
        if (!mc.player.isFallFlying() && !mc.player.isOnGround() && modeConfig.getValue() != FlyMode.PACKET
                && modeConfig.getValue() != FlyMode.BOUNCE && instantFlyConfig.getValue())
        {
            Managers.TICK.setClientTick(0.3f);
            if (mc.player.getVelocity().y < 0.0 && takeoffTimer.passed(1000))
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                takeoffTimer.reset();
            }
        }
        else
        {
            Managers.TICK.setClientTick(1.0f);
        }
        if (!lagTimer.passed(1000) && modeConfig.getValue() == FlyMode.CONTROL)
        {
            if (mc.player.isFallFlying())
            {
                mc.player.setVelocity(0.0, 0.0, 0.0);
                speed = 0.0f;
            }
            else if (mc.player.getVelocity().y < 0.0 && lagRedeployConfig.getValue() && redeployTimer.passed(100))
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                redeployTimer.reset();
            }
            return;
        }
        if (accelerationConfig.getValue())
        {
            if (!MovementUtil.isInputtingMovement() || mc.player.horizontalCollision)
            {
                speed = 0.0f;
            }
            else
            {
                speed += accelSpeedConfig.getValue();
            }
            if (speed >= maxSpeedConfig.getValue())
            {
                speed = maxSpeedConfig.getValue();
            }
        }
        else
        {
            speed = speedConfig.getValue();
        }
        if (fireworkConfig.getValue() && modeConfig.getValue() == FlyMode.CONTROL && mc.player.isFallFlying() && !isBoostedByRocket())
        {
            boostFirework();
        }
    }

    @EventListener
    public void onFallFlying(FallFlyingEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (modeConfig.getValue() == FlyMode.BOUNCE && bounceTimer.passed(5))
        {
            boolean fallFlying = event.isFallFlying();
            if (previousFallFlying && !fallFlying)
            {
                event.cancel();
                event.setFallFlying(recastElytra());
            }
            previousFallFlying = fallFlying;
            bounceTimer.reset();
        }
    }

    @EventListener
    public void onEntityTravel(EntityTravelEvent event)
    {
        if (mc.player == null || mc.world == null || event.getEntity() != mc.player || modeConfig.getValue() != FlyMode.BOUNCE)
        {
            return;
        }
        if (event.isPre())
        {
            prevPitch = mc.player.getPitch();
            mc.player.setPitch(pitchConfig.getValue());
        }
        else
        {
            mc.player.setPitch(prevPitch);
        }
    }

    @EventListener
    public void onTravel(TravelEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        switch (modeConfig.getValue())
        {
            case BOUNCE ->
            {
                if (boostConfig.getValue())
                {
                    if (!mc.player.isFallFlying() || mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.getHungerManager().getFoodLevel() <= 6.0f)
                    {
                        return;
                    }
                    boolean boost = mc.options.forwardKey.isPressed();
                    if (boost)
                    {
                        Vec3d glide = glideElytra(speedConfig.getValue() / 50.0f);
                        Vec3d motion = mc.player.getVelocity();
                        Managers.MOVEMENT.setMotionXZ(motion.x + glide.x, motion.z + glide.z);
                    }
                    Vec3d postMotion = mc.player.getVelocity();
                    double speed = Math.hypot(postMotion.x, postMotion.z);
                    if (speed > maxSpeedConfig.getValue())
                    {
                        Managers.MOVEMENT.setMotionXZ(postMotion.x * maxSpeedConfig.getValue() / speed,
                                postMotion.z * maxSpeedConfig.getValue() / speed);
                    }
                }
            }
            case CONTROL ->
            {
                if (!mc.player.isFallFlying())
                {
                    return;
                }
                event.cancel();
                float forward = mc.player.input.movementForward;
                float strafe = mc.player.input.movementSideways;
                float yaw = mc.player.getYaw();
                if (forward == 0.0f && strafe == 0.0f)
                {
                    Managers.MOVEMENT.setMotionXZ(0.0, 0.0);
                }
                else
                {
                    pitch = 12;
                    double rx = Math.cos(Math.toRadians(yaw + 90.0f));
                    double rz = Math.sin(Math.toRadians(yaw + 90.0f));
                    Managers.MOVEMENT.setMotionXZ(((forward * speed * rx)
                            + (strafe * speed * rz)), (forward * speed * rz)
                            - (strafe * speed * rx));
                }
                Managers.MOVEMENT.setMotionY(0.0);
                pitch = 0;
                if (mc.options.jumpKey.isPressed())
                {
                    pitch = -51;
                    Managers.MOVEMENT.setMotionY(vspeedConfig.getValue());
                }
                else if (mc.options.sneakKey.isPressed())
                {
                    Managers.MOVEMENT.setMotionY(-vspeedConfig.getValue());
                }
            }
            case BOOST ->
            {
                if (!mc.player.isFallFlying() || mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.getHungerManager().getFoodLevel() <= 6.0f)
                {
                    return;
                }
                // event.cancel();
                boolean boost = mc.options.jumpKey.isPressed();
                if (boost)
                {
                    Vec3d glide = glideElytra(speedConfig.getValue() / 50.0f);
                    Vec3d motion = mc.player.getVelocity();
                    Managers.MOVEMENT.setMotionXZ(motion.x + glide.x, motion.z + glide.z);
                }

                Vec3d postMotion = mc.player.getVelocity();
                double speed = Math.hypot(postMotion.x, postMotion.z);
                if (speed > maxSpeedConfig.getValue())
                {
                    Managers.MOVEMENT.setMotionXZ(postMotion.x * maxSpeedConfig.getValue() / speed,
                            postMotion.z * maxSpeedConfig.getValue() / speed);
                }
            }
            case FACTORIZE ->
            {
                if (!mc.player.isFallFlying() || mc.player.isTouchingWater() || mc.player.isInLava())
                {
                    return;
                }
                // event.cancel();
                boolean boost = mc.options.jumpKey.isPressed();
                // Vec3d glide = glideElytraVanilla(mc.player.getPitch());
                // Managers.MOVEMENT.setMotionXZ(glide.x, glide.z);
                float yaw = mc.player.getYaw() * 0.017453292f;
                if (boost)
                {
                    double sin = -MathHelper.sin(yaw);
                    double cos = MathHelper.cos(yaw);
                    double motionX = sin * (speedConfig.getValue() / 20.0f);
                    double motionZ = cos * (speedConfig.getValue() / 20.0f);
                    Managers.MOVEMENT.setMotionXZ(mc.player.getVelocity().x + motionX,
                            mc.player.getVelocity().z + motionZ);
                }
            }
        }
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (modeConfig.getValue() == FlyMode.PACKET)
        {
            if (mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA)
            {
                mc.player.getAbilities().flying = false;
                mc.player.getAbilities().setFlySpeed(0.05f);
                return;
            }
            event.cancel();
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed(speed / 15.0f);

            event.setY(0.0);
            if (mc.options.jumpKey.isPressed())
            {
                event.setY(vspeedConfig.getValue());
            }
            else if (mc.options.sneakKey.isPressed())
            {
                event.setY(-vspeedConfig.getValue());
            }
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (modeConfig.getValue() == FlyMode.BOUNCE)
        {
            pitch = pitchConfig.getValue();
            if (!mc.player.isSprinting())
            {
                mc.player.setSprinting(true);
            }
            if (!mc.player.isFallFlying())
            {
                mc.options.jumpKey.setPressed(true);
                if (mc.player.fallDistance > 0 && checkElytra())
                {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
            return;
        }
        if (modeConfig.getValue() != FlyMode.PACKET || mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA)
        {
            return;
        }
        if (infiniteDurabilityConfig.getValue() || !mc.player.isFallFlying())
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @EventListener
    public void onJumpDelay(JumpDelayEvent event)
    {
        if (modeConfig.getValue() != FlyMode.BOUNCE)
        {
            return;
        }
        event.cancel();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || !mc.player.isFallFlying())
        {
            return;
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && lagRedeployConfig.getValue())
        {
            lagTimer.reset();
            // re-equip elytra
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
            // mc.player.stopFallFlying();
            speed = 0.0f;
        }
        if (event.getPacket() instanceof EntityTrackerUpdateS2CPacket packet && packet.id() == mc.player.getId() && modeConfig.getValue() == FlyMode.PACKET)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook()
                && modeConfig.getValue() == FlyMode.BOUNCE)
        {
            ((AccessorPlayerMoveC2SPacket) packet).hookSetPitch(pitchConfig.getValue());
        }
        // We can predict the server firework lifetime here
        else if (event.getPacket() instanceof PlayerInteractItemC2SPacket && mc.player.isFallFlying())
        {
            ItemStack stack = mc.player.getMainHandStack();
            if (stack.getItem() instanceof FireworkRocketItem)
            {

            }
        }
    }

    private void boostFirework()
    {
        int slot = -1;
        ItemStack stack = null;
        for (int i = 0; i < 9; i++)
        {
            stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty())
            {
                continue;
            }
            if (stack.getItem() instanceof FireworkRocketItem)
            {
                slot = i;
                break;
            }
        }
        if (slot != -1)
        {
            int i = 1;
            if (stack.hasNbt())
            {
                i += stack.getOrCreateSubNbt("Fireworks").getByte("Flight");
            }
            fireworkLifetime = i * 10;
            Managers.INVENTORY.setSlot(slot);
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            Managers.INVENTORY.syncToClient();
        }
    }

    private boolean isBoostedByRocket()
    {
        return fireworkLifetime > 0 || ExtendedFireworkModule.getInstance().isExtendingFirework();
    }

    private Vec3d glideElytra(float speed)
    {
        float f3 = mc.player.getYaw();
        final double d4 = speed * Math.cos(Math.toRadians(f3 + 90.0f));
        final double d5 = speed * Math.sin(Math.toRadians(f3 + 90.0f));
        return new Vec3d(d4, 0.0, d5);
    }

    private Vec3d glideElytraVanilla(float pitch)
    {
        double d = 0.08;
        boolean bl = mc.player.getVelocity().y <= 0.0;
        if (bl && mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING))
        {
            d = 0.01;
        }
        Vec3d vec3d4 = mc.player.getVelocity();
        Vec3d vec3d5 = getRotationVector(pitch, mc.player.getYaw());
        float f = pitch * 0.017453292f;
        double i = Math.sqrt(vec3d5.x * vec3d5.x + vec3d5.z * vec3d5.z);
        double j = vec3d4.horizontalLength();
        double k = vec3d5.length();
        double l = Math.cos(f);
        l = l * l * Math.min(1.0, k / 0.4);
        vec3d4 = mc.player.getVelocity().add(0.0, d * (-1.0 + l * 0.75), 0.0);
        double m;
        // if (vec3d4.y < 0.0 && i > 0.0)
        // {
        //    m = vec3d4.y * -0.1 * l;
        //    vec3d4 = vec3d4.add(vec3d5.x * m / i, m, vec3d5.z * m / i);
        // }
        if (f < 0.0f && i > 0.0)
        {
            m = j * (double) (-MathHelper.sin(f)) * 0.04;
            vec3d4 = vec3d4.add(-vec3d5.x * m / i, m * 3.2, -vec3d5.z * m / i);
        }
        // if (i > 0.0)
        // {
        //     vec3d4 = vec3d4.add((vec3d5.x / i * j - vec3d4.x) * 0.1, 0.0, (vec3d5.z / i * j - vec3d4.z) * 0.1);
        // }
        return vec3d4.multiply(0.9900000095367432, 0.9800000190734863, 0.9900000095367432);
    }

    protected final Vec3d getRotationVector(float pitch, float yaw)
    {
        float f = pitch * 0.017453292f;
        float g = -yaw * 0.017453292f;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public boolean recastElytra()
    {
        if (checkElytra() && checkFallFlying())
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return true;
        }
        return false;
    }

    private boolean checkElytra()
    {
        if (mc.player.input.jumping && !mc.player.getAbilities().flying && !mc.player.hasVehicle() && !mc.player.isClimbing())
        {
            ItemStack itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            return itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack);
        }
        return false;
    }

    private boolean checkFallFlying()
    {
        if (!mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION))
        {
            ItemStack itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack))
            {
                mc.player.startFallFlying();
                return true;
            }
        }
        return false;
    }

    public enum FlyMode
    {
        CONTROL,
        BOOST,
        FACTORIZE,
        PACKET,
        BOUNCE
    }
}

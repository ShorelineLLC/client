package net.shoreline.client.impl.module.movement;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.MouseUpdateEvent;
import net.shoreline.client.impl.event.camera.CameraRotationEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.entity.player.PlayerMoveEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorFireworkRocketEntity;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class ElytraFlyModule extends RotationModule
{
    private static ElytraFlyModule INSTANCE;

    Config<FlyMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for elytra flight", FlyMode.CONTROL, FlyMode.values()));
    // Speeds
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f, () -> modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Float> vspeedConfig = register(new NumberConfig<>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f, () -> modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Boolean> accelerateConfig = register(new BooleanConfig("Accelerate", "Accelerates fly speed", false, () -> modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Float> accelSpeedConfig = register(new NumberConfig<>("AccelSpeed", "Acceleration speed", 0.01f, 0.1f, 1.00f, () -> accelerateConfig.getValue() && modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Float> maxSpeedConfig = register(new NumberConfig<>("MaxSpeed", "The maximum flight speed", 0.1f, 3.5f, 10.0f, () -> accelerateConfig.getValue() && modeConfig.getValue() != FlyMode.BOOST && modeConfig.getValue() != FlyMode.BOUNCE));
    Config<Boolean> boostConfig = register(new BooleanConfig("VanillaBoost", "Uses vanilla boost speed", false, () -> modeConfig.getValue() != FlyMode.BOOST));
    // Control
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates the player when moving", false, () -> modeConfig.getValue() == FlyMode.CONTROL));
    Config<Boolean> fireworkConfig = register(new BooleanConfig("Fireworks", "Uses fireworks when flying", false, () -> modeConfig.getValue() == FlyMode.CONTROL));
    // Bounce
    Config<Float> pitchConfig = register(new NumberConfig<>("Pitch", "The pitch angle of bounce", 70.0f, 80.0f, 90.0f, () -> modeConfig.getValue() == FlyMode.BOUNCE));
    Config<Boolean> obstaclePasserConfig = new BooleanConfig("ObstaclePasser", "Passes obstacles and resets fly using Baritone", modeConfig.getValue() == FlyMode.BOUNCE);

    private final static double GRIM_AIR_FRICTION = 0.0264444413;

    private boolean resetSpeed;
    private float speed;

    private float cameraPitch;

    public ElytraFlyModule()
    {
        super("ElytraFly", "Allows you to fly freely using an elytra", ModuleCategory.MOVEMENT);
        INSTANCE = this;
        if (ShorelineMod.isBaritonePresent())
        {
            register(obstaclePasserConfig);
        }
    }

    public static ElytraFlyModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onEnable()
    {
        resetSpeed = true;
    }

    @Override
    public void onDisable()
    {
        if (ShorelineMod.isBaritonePresent())
        {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().forceCancel();
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.POST &&
                event.getConfig() == accelerateConfig && accelerateConfig.getValue())
        {
            resetSpeed = true;
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
        {
            return;
        }

        if (!mc.player.isFallFlying())
        {
            return;
        }

        if (ShorelineMod.isBaritonePresent() && obstaclePasserConfig.getValue())
        {
            if (!BaritoneAPI.getSettings().freeLook.value)
            {
                ChatUtil.clientSendMessage(Formatting.RED + "Please enable FreeLook in Baritone to use ObstaclePasser!", 5005);
                return;
            }
            if (mc.player.horizontalCollision)
            {
                BlockPos goalBlockPos = null;
                for (int i = 3; i < 64; i++)
                {
                    Vec3d position = mc.player.getPos();
                    Vec3d vec3d2 = RotationUtil.getRotationVector(0.0f, mc.player.getYaw());
                    Vec3d vec3d3 = position.add(vec3d2.x * i, 0.0, vec3d2.z * i);
                    BlockPos blockPos = BlockPos.ofFloored(vec3d3);
                    if (mc.world.getBlockState(blockPos).isAir() && mc.world.getBlockState(blockPos.up()).isAir())
                    {
                        goalBlockPos = blockPos;
                        break;
                    }
                }
                if (goalBlockPos != null)
                {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess()
                            .setGoalAndPath(new GoalBlock(goalBlockPos.getX(), goalBlockPos.getY(), goalBlockPos.getZ()));
                }
            }
        }

        if (modeConfig.getValue() == FlyMode.CONTROL && rotateConfig.getValue())
        {
            float yaw = SprintModule.getInstance().getSprintYaw(mc.player.getYaw());
            setRotation(yaw, getControlPitch());

            int fireworkSlot = -1;
            for (int i = 0; i < 36; i++)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() instanceof FireworkRocketItem)
                {
                    fireworkSlot = i;
                    break;
                }
            }
            if (fireworkConfig.getValue() && fireworkSlot != -1)
            {
                if (fireworkSlot < 9)
                {
                    Managers.INVENTORY.setSlot(fireworkSlot);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    Managers.INVENTORY.syncToClient();
                }
                else
                {
                    mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
        else if (modeConfig.getValue() == FlyMode.BOUNCE)
        {
            if (canSprint())
            {
                mc.player.setSprinting(true);
            }
            mc.player.setPitch(pitchConfig.getValue());
            setRotation(mc.player.getYaw(), pitchConfig.getValue());
        }
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
        {
            return;
        }

        if (!mc.player.isFallFlying() || mc.player.isTouchingWater()
                || mc.player.isInLava() || mc.player.getHungerManager().getFoodLevel() <= 6.0f)
        {
            return;
        }

        if (modeConfig.getValue() == FlyMode.BOOST)
        {
            boolean boost = (mc.options.jumpKey.isPressed() || AutoWalkModule.getInstance().isEnabled());
            if (boost)
            {
                applyVanillaBoost();
            }
        }
        else if (boostConfig.getValue())
        {
            applyVanillaBoost();
        }
    }

    @EventListener
    public void onTravel(TravelEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
        {
            return;
        }

        if (!MovementUtil.isInputtingMovement() || mc.player.horizontalCollision)
        {
            resetSpeed = true;
        }

        if (accelerateConfig.getValue())
        {
            if (resetSpeed)
            {
                speed = 0.1f;
                resetSpeed = false;
            }
            if (speed < maxSpeedConfig.getValue())
            {
                speed += accelSpeedConfig.getValue();
            }
            if (speed - accelSpeedConfig.getValue() > maxSpeedConfig.getValue())
            {
                speed -= accelSpeedConfig.getValue();
            }
        }
        else
        {
            speed = speedConfig.getValue();
        }

        switch (modeConfig.getValue())
        {
            case CONTROL ->
            {
                if (!mc.player.isFallFlying())
                {
                    return;
                }
                event.cancel();
                float forward = mc.player.input.movementForward;
                float side = mc.player.input.movementSideways;
                float yaw = Managers.ROTATION.isRotating() ? Managers.ROTATION.getRotationYaw() : mc.player.getYaw();
                if (forward != 0.0f)
                {
                    if (side > 0.0f)
                    {
                        yaw += forward > 0.0f ? -45.0f : 45.0f;
                    }
                    else if (side < 0.0f)
                    {
                        yaw += forward > 0.0f ? 45.0f : -45.0f;
                    }
                    side = 0.0f;
                    if (forward > 0.0f)
                    {
                        forward = 1.0f;
                    }
                    else if (forward < 0.0f)
                    {
                        forward = -1.0f;
                    }
                }
                final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
                final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
                final double rx = forward * speed * cos + side * speed * sin;
                final double rz = forward * speed * sin - side * speed * cos;
                if (forward == 0.0f && side == 0.0f)
                {
                    Managers.MOVEMENT.setMotionXZ(0.0, 0.0);
                }
                else
                {
                    Managers.MOVEMENT.setMotionXZ(rx, rz);
                }
                Managers.MOVEMENT.setMotionY(0.0);
                if (mc.options.jumpKey.isPressed())
                {
                    Managers.MOVEMENT.setMotionY(vspeedConfig.getValue());
                }
                else if (mc.options.sneakKey.isPressed())
                {
                    Managers.MOVEMENT.setMotionY(-vspeedConfig.getValue());
                }
            }
            case BOUNCE ->
            {
                if (!mc.player.getInventory().getArmorStack(2).getItem().equals(Items.ELYTRA))
                {
                    return;
                }

                if (!mc.player.isFallFlying())
                {
                    Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startFallFlying();
                }
                if (mc.player.isOnGround())
                {
                    mc.player.jump();
                }
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
        {
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket)
        {
            resetSpeed = true;
        }
    }

    @EventListener
    public void onCameraRotation(CameraRotationEvent event)
    {
        if (ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
        {
            return;
        }

        if (modeConfig.getValue() == FlyMode.BOUNCE)
        {
            event.setYaw(event.getYaw());
            event.setPitch(cameraPitch);
        }
    }

    @EventListener
    public void onMouseUpdate(MouseUpdateEvent event)
    {
        if (ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
        {
            return;
        }

        if (modeConfig.getValue() == FlyMode.BOUNCE)
        {
            event.cancel();
            float f = (float) event.getCursorDeltaY() * 0.15F;
            float g = (float) event.getCursorDeltaX() * 0.15F;
            this.cameraPitch += f;
            mc.player.setYaw(mc.player.getYaw() + g);
            this.cameraPitch = MathHelper.clamp(cameraPitch, -90.0F, 90.0F);
        }
    }

    public float getControlPitch()
    {
        if (isBoostedByRocket())
        {
            if (mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed())
            {
                return MovementUtil.isMoving() ? -50.0f : -90.0f;
            }
            else if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed())
            {
                return MovementUtil.isMoving() ? 50.0f : 90.0f;
            }
            return 0.0f;
        }
        else
        {
            if (mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed())
            {
                return -50.0f;
            }
            else if (mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed())
            {
                return 50.0f;
            }
            return 0.1f;
        }
    }

    public void applyVanillaBoost()
    {
        float yaw = Managers.ROTATION.isRotating() ? Managers.ROTATION.getRotationYaw() : mc.player.getYaw();
        final double x = GRIM_AIR_FRICTION * Math.cos(Math.toRadians(yaw + 90.0f));
        final double z = GRIM_AIR_FRICTION * Math.sin(Math.toRadians(yaw + 90.0f));
        double motionX = mc.player.getVelocity().x + x;
        double motionZ = mc.player.getVelocity().z + z;
        mc.player.setVelocity(motionX, mc.player.getVelocity().y, motionZ);
    }

    private boolean isBoostedByRocket()
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (entity instanceof FireworkRocketEntity rocket
                    && ((AccessorFireworkRocketEntity) rocket).hookWasShotByEntity()
                    && ((AccessorFireworkRocketEntity) rocket).hookGetShooter() == mc.player)
            {
                return true;
            }
        }
        return false;
    }

    private boolean canSprint()
    {
        return !mc.player.isSneaking() && !mc.player.isRiding()
                && !mc.player.isFallFlying()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F;
    }

    public boolean isBounce()
    {
        return modeConfig.getValue() == FlyMode.BOUNCE;
    }

    public enum FlyMode
    {
        CONTROL,
        BOOST,
        BOUNCE,
        // PACKET
    }
}

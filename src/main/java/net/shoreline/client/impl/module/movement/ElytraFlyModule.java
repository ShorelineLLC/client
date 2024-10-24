package net.shoreline.client.impl.module.movement;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
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
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.EntityTravelEvent;
import net.shoreline.client.impl.event.entity.FallFlyingEvent;
import net.shoreline.client.impl.event.entity.JumpDelayEvent;
import net.shoreline.client.impl.event.entity.player.PlayerMoveEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.entity.ElytraTransformEvent;
import net.shoreline.client.impl.module.RotationModule;
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
    private static ElytraFlyModule INSTANCE;

    Config<FlyMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for elytra flight", FlyMode.CONTROL, FlyMode.values()));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f));
    Config<Float> vspeedConfig = register(new NumberConfig<>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f, () -> modeConfig.getValue() != FlyMode.BOOST));
    Config<Boolean> accelerationConfig = register(new BooleanConfig("Acceleration", "Accelerates fly speed", false, () -> modeConfig.getValue() != FlyMode.BOOST));
    Config<Float> accelSpeedConfig = register(new NumberConfig<>("AccelSpeed", "Acceleration speed", 0.01f, 0.21f, 1.00f, () -> accelerationConfig.getValue() && modeConfig.getValue() != FlyMode.BOOST));
    Config<Float> maxSpeedConfig = register(new NumberConfig<>("MaxSpeed", "The maximum flight speed", 0.1f, 3.5f, 10.0f, () -> accelerationConfig.getValue() || modeConfig.getValue() == FlyMode.BOOST));
    Config<Boolean> fireworkConfig = register(new BooleanConfig("Fireworks", "Uses fireworks when flying", false, () -> modeConfig.getValue() == FlyMode.CONTROL));
    Config<Boolean> fakeFlyConfig = register(new BooleanConfig("FakeFly", "Fly without showing your elytra", false, () -> modeConfig.getValue() == FlyMode.BOOST));

    private float speed;
    private float pitch;

    public ElytraFlyModule()
    {
        super("ElytraFly", "Allows you to fly freely using an elytra", ModuleCategory.MOVEMENT);
        INSTANCE = this;
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

    @EventListener
    public void onTravel(TravelEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        // TOOD: Accel impl
        speed = speedConfig.getValue();

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
                boolean boost = (mc.options.jumpKey.isPressed() || AutoWalkModule.getInstance().isEnabled());
                mc.player.setVelocity(mc.player.getVelocity().multiply(boost ? 1.0f + speedConfig.getValue() : 1.0f));
            }
        }
    }

    public enum FlyMode
    {
        CONTROL,
        BOOST
    }
}

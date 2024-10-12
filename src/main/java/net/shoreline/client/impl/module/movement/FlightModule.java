package net.shoreline.client.impl.module.movement;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.module.exploit.DisablerModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class FlightModule extends ToggleModule
{
    private static FlightModule INSTANCE;

    Config<FlightMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for vanilla flight", FlightMode.NORMAL, FlightMode.values()));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f));
    Config<Float> vspeedConfig = register(new NumberConfig<>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f));
    Config<AntiKick> antiKickConfig = register(new EnumConfig<>("AntiKick", "Prevents vanilla flight detection", AntiKick.NORMAL, AntiKick.values()));
    Config<Boolean> accelerateConfig = register(new BooleanConfig("Accelerate", "Accelerate as you fly", false));
    Config<Float> accelerateSpeedConfig = register(new NumberConfig<>("AccelerateSpeed", "Speed to accelerate as", 0.01f, 0.2f, 1.0f, () -> accelerateConfig.getValue()));
    Config<Float> maxSpeedConfig = register(new NumberConfig<>("MaxSpeed", "Max speed to acceleratee to", 1.0f, 5.0f, 10.0f, () -> accelerateConfig.getValue()));

    private double speed;
    // antikick bs
    private double lastY;
    private boolean floating;
    private int floatingTicks;
    private boolean modifyY;

    public FlightModule()
    {
        super("Flight", "Allows the player to fly in survival", ModuleCategory.MOVEMENT);
        INSTANCE = this;
    }

    public static FlightModule getInstance()
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
        if (modeConfig.getValue() == FlightMode.VANILLA)
        {
            enableVanillaFly();
        }
        speed = 0.0;
    }

    @Override
    public void onDisable()
    {
        if (modeConfig.getValue() == FlightMode.VANILLA)
        {
            disableVanillaFly();
        }
        modifyY = false;
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (accelerateConfig.getValue())
        {
            if (!MovementUtil.isInputtingMovement() || mc.player.horizontalCollision)
            {
                speed = 0.0f;
            }
            speed += accelerateSpeedConfig.getValue();
            if (speed > maxSpeedConfig.getValue())
            {
                speed = maxSpeedConfig.getValue();
            }
        }
        else
        {
            speed = speedConfig.getValue();
        }
        if (DisablerModule.getInstance().grimFireworkCheck())
        {
            if (modeConfig.getValue() == FlightMode.VANILLA)
            {
                disableVanillaFly();
            }
            return;
        }
        if (modeConfig.getValue().equals(FlightMode.VANILLA))
        {
            enableVanillaFly();
            mc.player.getAbilities().setFlySpeed((float) (speed * 0.05f));
        }
        else
        {
            disableVanillaFly();
        }

        boolean stopVerticalMovement = false;
        if (floating)
        {
            floatingTicks++;
            // Vanilla fly kick checks every 80 ticks
            if (floatingTicks >= 20)
            {
                if (antiKickConfig.getValue() == AntiKick.PACKET)
                {
                    modifyY = true;
                }
                else if (antiKickConfig.getValue() == AntiKick.NORMAL)
                {
                    mc.player.setPosition(mc.player.getX(), mc.player.getY() - 0.0313, mc.player.getZ());
                    if (modeConfig.getValue() == FlightMode.VANILLA)
                    {
                        disableVanillaFly();
                        Managers.MOVEMENT.setMotionY(0.0);
                        stopVerticalMovement = true;
                    }
                }
                floatingTicks = 0;
                floating = false;
            }
        }

        if (modeConfig.getValue() == FlightMode.NORMAL)
        {
            Managers.MOVEMENT.setMotionY(0.0);
            if (mc.options.jumpKey.isPressed() && !stopVerticalMovement)
            {
                Managers.MOVEMENT.setMotionY(vspeedConfig.getValue());
            }
            else if (mc.options.sneakKey.isPressed())
            {
                Managers.MOVEMENT.setMotionY(-vspeedConfig.getValue());
            }
            speed = Math.max(speed, 0.2873f);
            float forward = mc.player.input.movementForward;
            float strafe = mc.player.input.movementSideways;
            float yaw = mc.player.getYaw();
            if (forward == 0.0f && strafe == 0.0f)
            {
                Managers.MOVEMENT.setMotionXZ(0.0f, 0.0f);
                return;
            }
            double rx = Math.cos(Math.toRadians(yaw + 90.0f));
            double rz = Math.sin(Math.toRadians(yaw + 90.0f));
            Managers.MOVEMENT.setMotionXZ((forward * speed * rx) + (strafe * speed * rz),
                    (forward * speed * rz) - (strafe * speed * rx));
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && antiKickConfig.getValue() != AntiKick.OFF)
        {
            final double packetY = packet.getY(Double.NaN);
            if (!Double.isNaN(packetY))
            {
                if (modifyY)
                {
                    ((AccessorPlayerMoveC2SPacket) packet).hookSetY(lastY - 0.04);
                    modifyY = false;
                    return;
                }
                floating = floatingCheck(packet);
                lastY = packetY;
                return;
            }

            if (modifyY)
            {
                PlayerMoveC2SPacket packet1 = packet.changesLook() ? new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() - 0.04, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), packet.isOnGround()) :
                        new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.04, mc.player.getZ(), packet.isOnGround());
                event.cancel();
                Managers.NETWORK.sendQuietPacket(packet1);
                modifyY = false;
            }
        }
    }

    private boolean floatingCheck(PlayerMoveC2SPacket packet)
    {
        double e = MathHelper.clamp(packet.getY(mc.player.getY()), -2.0E7, 2.0E7);
        double s = e - lastY;
        return s >= -0.03125 && !mc.player.groundCollision && isEntityOnAir(mc.player);
    }

    private boolean isEntityOnAir(Entity entity)
    {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    private void enableVanillaFly()
    {
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
    }

    private void disableVanillaFly()
    {
        if (!mc.player.isCreative())
        {
            mc.player.getAbilities().allowFlying = false;
        }
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05f);
    }

    public enum FlightMode
    {
        NORMAL,
        VANILLA
    }

    public enum AntiKick
    {
        NORMAL,
        PACKET,
        OFF
    }
}

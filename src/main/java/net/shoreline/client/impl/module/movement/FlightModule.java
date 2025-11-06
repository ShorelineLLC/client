package net.shoreline.client.impl.module.movement;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.imixin.IPlayerMoveC2SPacket;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

public class FlightModule extends MovementModule
{
    public static FlightModule INSTANCE;

    Config<FlightMode> modeConfig = new EnumConfig.Builder<FlightMode>("Mode")
            .setValues(FlightMode.values())
            .setDefaultValue(FlightMode.NORMAL).build();
    Config<Float> speedConfig = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.1f).setMax(10.0f).setDefaultValue(2.5f)
            .setDescription("Movement speed").build();
    Config<Float> vSpeedConfig = new NumberConfig.Builder<Float>("Vertical")
            .setMin(0.1f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Vertical movement speed").build();
    Config<AntiKick> antiKickConfig = new EnumConfig.Builder<AntiKick>("AntiKick")
            .setValues(AntiKick.values())
            .setDescription("Prevents getting kicked by vanilla flight detection")
            .setDefaultValue(AntiKick.OFF).build();

    private double speed;
    // antikick bs
    private double lastY;
    private boolean floating;
    private int floatingTicks;
    private boolean modifyY;

    public FlightModule()
    {
        super("Flight", "Fly like a bird", GuiCategory.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return Formatter.formatEnum(modeConfig.getValue());
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
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        speed = speedConfig.getValue();
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
                        setMotionY(0.0);
                        stopVerticalMovement = true;
                    }
                }

                floatingTicks = 0;
                floating = false;
            }
        }

        if (modeConfig.getValue() == FlightMode.NORMAL)
        {
            setMotionY(0.0);
            if (mc.options.jumpKey.isPressed() && !stopVerticalMovement)
            {
                setMotionY(vSpeedConfig.getValue());
            }
            else if (mc.options.sneakKey.isPressed())
            {
                setMotionY(-vSpeedConfig.getValue());
            }

            Vec2f strafeVec = strafe(speedConfig.getValue());
            setMotionXZ(strafeVec.x, strafeVec.y);
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull())
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
                    ((IPlayerMoveC2SPacket) packet).setY(lastY - 0.04);
                    modifyY = false;
                    return;
                }

                floating = floatingCheck(packet);
                lastY = packetY;
                return;
            }

            if (modifyY)
            {
                PlayerMoveC2SPacket packet1 = packet.changesLook() ?
                        new PlayerMoveC2SPacket.Full(
                                mc.player.getX(),
                                mc.player.getY() - 0.04,
                                mc.player.getZ(),
                                mc.player.getYaw(),
                                mc.player.getPitch(),
                                packet.isOnGround(),
                                mc.player.horizontalCollision) :
                        new PlayerMoveC2SPacket.PositionAndOnGround(
                                mc.player.getX(),
                                mc.player.getY() - 0.04,
                                mc.player.getZ(),
                                packet.isOnGround(),
                                mc.player.horizontalCollision);

                event.cancel();
                sendQuietPacket(packet1);
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

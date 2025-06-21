package net.shoreline.client.impl.module.movement;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.util.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;

public class VelocityModule extends Toggleable
{
    Config<VelocityMode> modeConfig = new EnumConfig.Builder<VelocityMode>("Mode")
            .setValues(VelocityMode.values())
            .setDescription("The bypass mode for anti knockback")
            .setDefaultValue(VelocityMode.NORMAL).build();
    Config<Integer> horizontalConfig = new NumberConfig.Builder<Integer>("Horizontal")
            .setDefaultValue(0).setMin(0).setMax(100)
            .setFormat(NumberFormat.PERCENT)
            .setVisible(() -> modeConfig.getValue() == VelocityMode.NORMAL)
            .setDescription("The horizontal velocity reduction").build();
    Config<Integer> verticalConfig = new NumberConfig.Builder<Integer>("Vertical")
            .setDefaultValue(0).setMin(0).setMax(100)
            .setFormat(NumberFormat.PERCENT)
            .setVisible(() -> modeConfig.getValue() == VelocityMode.NORMAL)
            .setDescription("The vertical velocity reduction").build();
    Config<Boolean> groundOnlyConfig = new BooleanConfig.Builder("GroundOnly")
            .setDescription("Only applies wall velocity when grounded.")
            .setVisible(() -> modeConfig.getValue().equals(VelocityMode.WALLS))
            .setDefaultValue(false).build();

    private boolean concealVelocity;
    private boolean cancelVelocity;

    private final DecimalFormat percentFormat = new DecimalFormat("0.0");

    public VelocityModule()
    {
        super("Velocity", new String[] {"AntiKB"}, "Prevents player knockback", GuiCategory.MOVEMENT);
    }

    @Override
    public String getModuleData()
    {
        if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            return String.format("H:%s%%, V:%s%%",
                    percentFormat.format(horizontalConfig.getValue()),
                    percentFormat.format(verticalConfig.getValue()));
        }

        return Formatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet
                && packet.getEntityId() == mc.player.getId())
        {
            if (concealVelocity && packet.getVelocityX() == 0 && packet.getVelocityZ() == 0 && packet.getVelocityZ() == 0)
            {
                concealVelocity = false;
                return;
            }

            if (shouldCancelKnockback())
            {
                event.cancel();
            } else
            {

            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet)
        {
            if (shouldCancelExplosions())
            {
                event.cancel();
            } else
            {

            }
        }
    }

    public boolean shouldCancelKnockback()
    {
        if (modeConfig.getValue() == VelocityMode.WALLS)
        {
            return isInsideWall() && (!groundOnlyConfig.getValue() || mc.player.isOnGround());
        } else if (modeConfig.getValue() == VelocityMode.GRIM_V2)
        {

        }

        return true;
    }

    public boolean shouldCancelExplosions()
    {
        if (modeConfig.getValue() == VelocityMode.WALLS)
        {
            return isInsideWall();
        } else if (modeConfig.getValue() == VelocityMode.GRIM_V2)
        {

        }

        return true;
    }

    private boolean isInsideWall()
    {
        return BlockPos.stream(mc.player.getBoundingBox())
                .anyMatch(blockPos -> !mc.world.getBlockState(blockPos).isReplaceable());
    }

    private enum VelocityMode
    {
        NORMAL,
        WALLS,
        GRIM_V2
    }
}

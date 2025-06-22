package net.shoreline.client.impl.module.movement;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.ExplosionEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket;
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
    Config<Boolean> concealConfig = new BooleanConfig.Builder("Conceal")
            .setDescription("Prevents excessive lagbacks on servers with strict movement anticheats")
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

    @Override
    public void onDisable()
    {
        concealVelocity = false;
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
            } else if (modeConfig.getValue() == VelocityMode.NORMAL)
            {
                double e = packet.getVelocityX() * (horizontalConfig.getValue() / 100.0f);
                double f = packet.getVelocityY() * (verticalConfig.getValue() / 100.0f);
                double g = packet.getVelocityZ() * (horizontalConfig.getValue() / 100.0f);
                ((AccessorEntityVelocityUpdateS2CPacket) packet).setX((int) (e * 8000.0));
                ((AccessorEntityVelocityUpdateS2CPacket) packet).setY((int) (f * 8000.0));
                ((AccessorEntityVelocityUpdateS2CPacket) packet).setZ((int) (g * 8000.0));
            }
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && concealConfig.getValue())
        {
            concealVelocity = true;
        }
    }

    @EventListener
    public void onExplosion(ExplosionEvent event)
    {
        if (shouldCancelExplosions())
        {
            event.cancel();
            event.setPlayerVelocity(Vec3d.ZERO);
        } else if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            Vec3d knockback = event.getPlayerVelocity();
            double e = knockback.x * (horizontalConfig.getValue() / 100.0f);
            double f = knockback.y * (verticalConfig.getValue() / 100.0f);
            double g = knockback.z * (horizontalConfig.getValue() / 100.0f);
            event.cancel();
            event.setPlayerVelocity(new Vec3d(e, f, g));
        }
    }

    public boolean shouldCancelKnockback()
    {
        if (modeConfig.getValue() == VelocityMode.WALLS)
        {
            return isInsideWall() && (!groundOnlyConfig.getValue() || mc.player.isOnGround());
        } else if (modeConfig.getValue() == VelocityMode.GRIM_V2)
        {

        } else if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            return horizontalConfig.getValue() == 0 && verticalConfig.getValue() == 0;
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

        } else if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            return horizontalConfig.getValue() == 0 && verticalConfig.getValue() == 0;
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

package net.shoreline.client.impl.module.movement;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class VelocityModule extends Toggleable
{
    Config<Number> horizontalConfig = new NumberConfig.Builder<>("Horizontal")
            .setDefaultValue(0).setMin(0).setMax(100)
            .setDescription("The horizontal velocity reduction").build();
    Config<Number> verticalConfig = new NumberConfig.Builder<>("Vertical")
            .setDefaultValue(0).setMin(0).setMax(100)
            .setDescription("The vertical velocity reduction").build();

    public VelocityModule()
    {
        super("Velocity", "Prevents player knockback", GuiCategory.MOVEMENT);
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
        return true;
    }

    public boolean shouldCancelExplosions()
    {
        return true;
    }
}

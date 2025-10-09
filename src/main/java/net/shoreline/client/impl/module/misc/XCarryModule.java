package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class XCarryModule extends Toggleable
{
    public XCarryModule()
    {
        super("XCarry", "Carry items in the crafting slots", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof CloseHandledScreenC2SPacket packet
                && packet.getSyncId() == mc.player.playerScreenHandler.syncId)
        {
            event.cancel();
        }
    }
}

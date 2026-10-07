package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.util.math.PerSecond;
import net.shoreline.eventbus.annotation.EventListener;

public class PacketsHudModule extends DynamicHudModule
{
    private final PerSecond incoming = new PerSecond();
    private final PerSecond outgoing = new PerSecond();

    public PacketsHudModule()
    {
        super("Packets", "Displays the incoming and outgoing packets", 200, 275);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getPacketsText, () -> true));
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        incoming.count();
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        outgoing.count();
    }

    public String getPacketsText()
    {
        return String.format("Packets " + Formatting.WHITE + "%s<-%s", outgoing.getPerSecond(), incoming.getPerSecond());
    }
}

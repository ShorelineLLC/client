package net.shoreline.client.impl.world;

import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.util.math.QueueAverage;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class TickManager extends GenericFeature
{
    private final QueueAverage ticks = new QueueAverage(20);
    private long lastUpdate;

    public TickManager()
    {
        super("Ticks");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onDisconnect(WorldEvent.Disconnect event)
    {
        ticks.clear();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
        {
            float last = 20000.0f / (System.currentTimeMillis() - lastUpdate);
            ticks.add(last);
            lastUpdate = System.currentTimeMillis();
        }
    }

    public int getSize()
    {
        return ticks.size();
    }

    public double getAverageTPS()
    {
        return ticks.average();
    }

    public double getLatestTPS()
    {
        return ticks.latest();
    }
}

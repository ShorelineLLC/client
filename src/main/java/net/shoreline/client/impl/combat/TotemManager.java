package net.shoreline.client.impl.combat;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TotemManager extends GenericFeature
{
    private final ConcurrentMap<UUID, TotemData> totems = new ConcurrentHashMap<>();

    public TotemManager()
    {
        super("Totem");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket p)
        {
            Entity entity = p.getEntity(mc.world);
            if (entity == null)
            {
                return;
            }

            switch (p.getStatus())
            {
                case 3 ->
                {
                    EventBus.INSTANCE.dispatch(new EntityDeathEvent(entity));
                    totems.remove(entity.getUuid());
                }
                case 35 ->
                {
                    if (entity.isAlive())
                    {
                        totems.compute(entity.getUuid(), (uuid, data) ->
                                new TotemData(System.currentTimeMillis(), data == null ? 1 : data.getPops() + 1));
                    }
                }
            }
        }
    }

    @EventListener
    public void onDisconnect(WorldEvent.Disconnect event)
    {
        totems.clear();
    }

    public int getTotems(Entity entity)
    {
        return totems.getOrDefault(entity.getUuid(), TotemData.EMPTY).getPops();
    }

    public long getLastPopTime(Entity entity)
    {
        return totems.getOrDefault(entity.getUuid(), TotemData.EMPTY).getLastPopTime();
    }

    @Getter
    public static class TotemData
    {
        public static final TotemData EMPTY = new TotemData(-1, 0);

        private final long lastPopTime;
        private final int pops;

        public TotemData(long lastPopTime, int pops)
        {
            this.lastPopTime = lastPopTime;
            this.pops = pops;
        }
    }
}

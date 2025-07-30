package net.shoreline.client.impl.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
        if (event.getPacket() instanceof EntityStatusS2CPacket p && mc.world != null)
        {
            Entity entity = p.getEntity(mc.world);
            if (entity != null)
            {
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
                            if (totems.containsKey(entity.getUuid()))
                            {
                                totems.replace(entity.getUuid(), new TotemData(System.currentTimeMillis(),
                                        totems.get(entity.getUuid()).getPops() + 1));
                            }
                            else
                            {
                                totems.put(entity.getUuid(), new TotemData(System.currentTimeMillis(), 1));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onRemoveEntity(EntityDeathEvent event)
    {
        totems.remove(event.getEntity().getUuid());
    }

    @EventListener
    public void onDisconnect(WorldEvent.Disconnect event)
    {
        totems.clear();
    }

    public int getTotems(Entity entity)
    {
        return totems.getOrDefault(entity.getUuid(), new TotemData(0, 0)).getPops();
    }

    public long getLastPopTime(Entity entity)
    {
        return totems.getOrDefault(entity.getUuid(), new TotemData(-1, 0)).getLastPopTime();
    }

    public static class TotemData
    {
        private final long lastPopTime;
        private final int pops;

        public TotemData(long lastPopTime, int pops)
        {
            this.lastPopTime = lastPopTime;
            this.pops = pops;
        }

        public int getPops()
        {
            return pops;
        }

        public long getLastPopTime()
        {
            return lastPopTime;
        }
    }
}

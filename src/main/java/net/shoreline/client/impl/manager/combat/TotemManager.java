package net.shoreline.client.impl.manager.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author linus
 * @since 1.0
 */
public class TotemManager implements Globals
{
    //
    private final ConcurrentMap<UUID, TotemData> totems = new ConcurrentHashMap<>();

    /**
     *
     */
    public TotemManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING)
        {
            Entity entity = packet.getEntity(mc.world);
            if (entity != null && entity.isAlive())
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

    @EventListener(priority = Integer.MIN_VALUE)
    public void onRemoveEntity(EntityDeathEvent event)
    {
        totems.remove(event.getEntity().getUuid());
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        totems.clear();
    }

    /**
     * Returns the number of totems popped by the given {@link PlayerEntity}
     *
     * @param entity
     * @return Ehe number of totems popped by the player
     */
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

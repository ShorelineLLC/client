package net.shoreline.client.impl.manager.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitboxManager implements Globals
{
    private final List<Entity> serverCrawling = new CopyOnWriteArrayList<>();

    public HitboxManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (event.getPacket() instanceof EntityTrackerUpdateS2CPacket packet)
        {
            Entity entity = mc.world.getEntityById(packet.id());
            if (!(entity instanceof PlayerEntity))
            {
                return;
            }

            for (DataTracker.SerializedEntry<?> serializedEntry : packet.trackedValues())
            {
                DataTracker.Entry<?> entry = entity.getDataTracker().entries[serializedEntry.id()];
                if (!entry.getData().equals(Entity.POSE))
                {
                    continue;
                }

                if (!serializedEntry.value().equals(EntityPose.SWIMMING))
                {
                    serverCrawling.remove(entity);
                    continue;
                }

                if (!serverCrawling.contains(entity))
                {
                    serverCrawling.add(entity);
                }
            }
        }
    }

    public boolean isServerCrawling(Entity entity)
    {
        return serverCrawling.contains(entity);
    }

    public Box getCrawlingBoundingBox(Entity entity)
    {
        return entity.getDimensions(EntityPose.SWIMMING).getBoxAt(entity.getPos());
    }
}

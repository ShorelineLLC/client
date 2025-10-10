package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.shoreline.client.api.LoggingFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IEntity;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrystalOptimizer extends LoggingFeature
{
    private final ConcurrentMap<Integer, GhostCrystal> deadCrystals = new ConcurrentHashMap<>();

    public CrystalOptimizer()
    {
        super("Crystal Optimizer");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
//        for (Map.Entry<Integer, GhostCrystal> crystalData : deadCrystals.entrySet())
//        {
//            long timeSince = System.currentTimeMillis() - crystalData.getValue().timestamp;
//            if (timeSince > 1000L)
//            {
//                deadCrystals.remove(crystalData.getKey());
//
//                Entity crystalEntity = crystalData.getValue().crystalEntity();
//                Entity entity = mc.world.getEntityById(crystalEntity.getId());
//                if (entity == crystalEntity && !entity.isRemoved())
//                {
//                    continue;
//                }
//
//                if (entity != null && entity != crystalEntity)
//                {
//                    mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
//                }
//
//                if (crystalEntity.isRemoved())
//                {
//                    ((IEntity) crystalEntity).invokeUnsetRemoved();
//                    crystalEntity.setId(crystalData.getKey());
//
//                    mc.world.addEntity(crystalEntity);
//                }
//            }
//        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                deadCrystals.remove(id);
            }
        }
    }

    public void setDead(EndCrystalEntity crystalEntity)
    {
        mc.world.removeEntity(crystalEntity.getId(), Entity.RemovalReason.DISCARDED);
        deadCrystals.putIfAbsent(crystalEntity.getId(), new GhostCrystal(crystalEntity, System.currentTimeMillis()));
    }

    private record GhostCrystal(EndCrystalEntity crystalEntity, long timestamp) {}
}

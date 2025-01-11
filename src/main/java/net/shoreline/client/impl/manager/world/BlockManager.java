package net.shoreline.client.impl.manager.world;

import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.resource.metadata.BlockEntry;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.world.SpeedmineModule;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockManager implements Globals
{
    private final List<BreakEntry> breakPositions = new CopyOnWriteArrayList<>();

    public BlockManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTick(PlayerUpdateEvent event)
    {
        if (mc.player == null || mc.world == null)
        {
            breakPositions.clear();
            return;
        }


        for (BreakEntry blockEntry : breakPositions)
        {
            blockEntry.updateDamage();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet)
        {
            if (countBreaks(packet.getEntityId()) >= 2)
            {
                breakPositions.stream().filter(d -> d.getEntityId() == packet.getEntityId())
                        .min(Comparator.comparingLong(BreakEntry::getStartTime)).ifPresent(breakPositions::remove);
            }
            BreakEntry data = new BreakEntry(packet.getEntityId(), packet.getPos());
            data.startMining();
            breakPositions.add(data);
        }
    }

    public long countBreaks(int entityId)
    {
        return breakPositions.stream().filter(d -> d.getEntityId() == entityId).count();
    }

    public boolean isBreaking(BlockPos pos)
    {
        return breakPositions.stream().anyMatch(d -> d.getPos().equals(pos));
    }

    public boolean isPassed(BlockPos pos, float blockDamage)
    {
        return breakPositions.stream().anyMatch(d -> d.getPos().equals(pos) && d.getBlockDamage() >= blockDamage);
    }

    public static class BreakEntry
    {
        private final int entityId;
        private final BlockPos pos;
        private long startTime;
        private float blockDamage;
        private boolean started;

        public BreakEntry(int entityId, BlockPos pos)
        {
            this.entityId = entityId;
            this.pos = pos;
        }

        public void updateDamage()
        {
            if (started)
            {
                blockDamage += SpeedmineModule.getInstance().calcBlockBreakingDelta(
                        mc.world.getBlockState(pos), mc.world, pos);
            }
        }

        public void startMining()
        {
            started = true;
            startTime = System.currentTimeMillis();
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public float getBlockDamage()
        {
            return Math.min(blockDamage, 1.0f);
        }

        public int getEntityId()
        {
            return entityId;
        }

        public long getStartTime()
        {
            return startTime;
        }
    }
}

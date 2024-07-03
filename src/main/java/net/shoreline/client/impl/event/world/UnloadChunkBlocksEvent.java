package net.shoreline.client.impl.event.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.shoreline.eventbus.event.Event;

public class UnloadChunkBlocksEvent extends Event
{
    private final WorldChunk chunk;

    public UnloadChunkBlocksEvent(WorldChunk chunk)
    {
        this.chunk = chunk;
    }

    public boolean contains(BlockPos blockPos)
    {
        int chunkX = blockPos.getX() >> 4;
        int chunkZ = blockPos.getZ() >> 4;
        return chunkX == chunk.getPos().x && chunkZ == chunk.getPos().z;
    }
}

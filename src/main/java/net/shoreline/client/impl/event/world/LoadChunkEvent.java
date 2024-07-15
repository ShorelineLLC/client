package net.shoreline.client.impl.event.world;

import net.minecraft.world.chunk.Chunk;
import net.shoreline.eventbus.event.Event;

public class LoadChunkEvent extends Event
{
    private final Chunk chunk;

    public LoadChunkEvent(Chunk chunk)
    {
        this.chunk = chunk;
    }

    public Chunk getChunk()
    {
        return chunk;
    }
}

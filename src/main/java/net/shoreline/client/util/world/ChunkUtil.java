package net.shoreline.client.util.world;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@UtilityClass
public class ChunkUtil
{
    public boolean isLoaded(BlockPos pos)
    {
        int x = ChunkSectionPos.getSectionCoord(pos.getX());
        int z = ChunkSectionPos.getSectionCoord(pos.getZ());
        return MinecraftClient.getInstance().world.getChunkManager().isChunkLoaded(x, z);
    }
}

package net.shoreline.client.util.world;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.util.Globals;

import java.util.Set;

public class BlastResistantBlocks implements Globals
{
    // All blocks that are resistant to explosions
    private static final Set<Block> BLAST_RESISTANT = new ReferenceOpenHashSet<>(Set.of(
            Blocks.OBSIDIAN,
            Blocks.ANVIL,
            Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST,
            Blocks.BEACON
    ));
    // All blocks that are unbreakable with tools in survival mode
    private static final Set<Block> UNBREAKABLE = new ReferenceOpenHashSet<>(Set.of(
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.END_PORTAL_FRAME,
            Blocks.BARRIER
    ));

    /**
     * @param pos
     * @return
     */
    public static boolean isBreakable(BlockPos pos)
    {
        if (mc.world == null)
        {
            return false;
        }
        return isBreakable(mc.world.getBlockState(pos).getBlock());
    }

    /**
     * Returns <tt>true</tt> if the {@link BlockState} of the mining block is
     * breakable in survival mode
     *
     * @param block The block state of the mining block
     * @return <tt>true</tt> if the mining block is breakable
     */
    public static boolean isBreakable(Block block)
    {
        return !UNBREAKABLE.contains(block);
    }

    /**
     * @param pos
     * @return
     */
    public static boolean isUnbreakable(BlockPos pos)
    {
        if (mc.world == null)
        {
            return false;
        }
        return isUnbreakable(mc.world.getBlockState(pos).getBlock());
    }

    /**
     * @param block
     * @return
     */
    public static boolean isUnbreakable(Block block)
    {
        return UNBREAKABLE.contains(block);
    }

    /**
     * @param pos
     * @return
     */
    public static boolean isBlastResistant(BlockPos pos)
    {
        if (mc.world == null)
        {
            return false;
        }
        return isBlastResistant(mc.world.getBlockState(pos).getBlock());
    }

    /**
     * @param state
     * @return
     */
    public static boolean isBlastResistant(BlockState state)
    {
        return isBlastResistant(state.getBlock());
    }


    /**
     * @param block
     * @return
     */
    public static boolean isBlastResistant(Block block)
    {
        return BLAST_RESISTANT.contains(block);
    }
}

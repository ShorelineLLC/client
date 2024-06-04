package net.shoreline.client.util.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Set;

/**
 * @author linus
 * @since 1.0
 */
public class SneakBlocks
{
    // Constant set containing the blocks that can only be placed on if the
    // player is holding shift
    private static final Set<Block> SNEAK_BLOCKS;

    static
    {
        SNEAK_BLOCKS = Set.of(
                Blocks.CHEST,
                Blocks.ENDER_CHEST,
                Blocks.TRAPPED_CHEST,
                Blocks.CRAFTING_TABLE,
                Blocks.FURNACE,
                Blocks.BLAST_FURNACE,
                Blocks.FLETCHING_TABLE,
                Blocks.CARTOGRAPHY_TABLE,
                Blocks.ENCHANTING_TABLE,
                Blocks.SMITHING_TABLE,
                Blocks.STONECUTTER,
                Blocks.ANVIL,
                Blocks.CHIPPED_ANVIL,
                Blocks.DAMAGED_ANVIL,
                Blocks.JUKEBOX,
                Blocks.NOTE_BLOCK
        );
    }

    /**
     * Returns <tt>true</tt> if the block state can only be placed on if the
     * player is holding shift
     *
     * @return <tt>true</tt> if the block state requires sneaking to be
     * placed on
     */
    public static boolean isSneakBlock(BlockState state)
    {
        return isSneakBlock(state.getBlock());
    }

    /**
     * Returns <tt>true</tt> if the block can only be placed on if the player
     * is holding shift
     *
     * @return <tt>true</tt> if the block requires sneaking to be placed on
     */
    public static boolean isSneakBlock(Block block)
    {
        return SNEAK_BLOCKS.contains(block);
    }
}

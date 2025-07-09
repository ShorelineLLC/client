package net.shoreline.client.util.world;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

@UtilityClass
public class BlockUtil
{
    private final Set<Block> INTERACTABLE_BLOCKS = Set.of(
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
            Blocks.JUKEBOX,
            Blocks.NOTE_BLOCK,
            Blocks.SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.ACACIA_TRAPDOOR,
            Blocks.BAMBOO_TRAPDOOR,
            Blocks.BIRCH_TRAPDOOR,
            Blocks.CHERRY_TRAPDOOR,
            Blocks.COPPER_TRAPDOOR,
            Blocks.EXPOSED_COPPER_TRAPDOOR,
            Blocks.OXIDIZED_COPPER_TRAPDOOR,
            Blocks.WAXED_COPPER_TRAPDOOR,
            Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
            Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR,
            Blocks.WEATHERED_COPPER_TRAPDOOR,
            Blocks.SPRUCE_TRAPDOOR,
            Blocks.WARPED_TRAPDOOR,
            Blocks.IRON_TRAPDOOR,
            Blocks.DARK_OAK_TRAPDOOR,
            Blocks.JUNGLE_TRAPDOOR,
            Blocks.MANGROVE_TRAPDOOR,
            Blocks.OAK_TRAPDOOR,
            Blocks.CRIMSON_TRAPDOOR
    );

    public boolean isInteractable(BlockPos blockPos)
    {
        return isInteractable(MinecraftClient.getInstance().world.getBlockState(blockPos));
    }

    public boolean isInteractable(BlockState state)
    {
        return INTERACTABLE_BLOCKS.contains(state.getBlock());
    }

    public boolean isInteractable(Block block)
    {
        return INTERACTABLE_BLOCKS.contains(block);
    }
}

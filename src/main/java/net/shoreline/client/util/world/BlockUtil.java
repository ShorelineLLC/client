package net.shoreline.client.util.world;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Set;

@UtilityClass
public class BlockUtil
{
    private final Set<Block> INTERACTABLE_BLOCKS = new ReferenceOpenHashSet<>(Set.of(
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
    ));

    // All blocks that are resistant to explosions
    private final Set<Block> EXPLOSION_RESISTANT = new ReferenceOpenHashSet<>(Set.of(
            Blocks.OBSIDIAN,
            Blocks.ANVIL,
            Blocks.ENCHANTING_TABLE,
            Blocks.ENDER_CHEST,
            Blocks.BEACON
    ));

    // All blocks that are unbreakable with tools in survival mode
    private final Set<Block> UNBREAKABLE = new ReferenceOpenHashSet<>(Set.of(
            Blocks.BEDROCK,
            Blocks.BARRIER
    ));

    public boolean isInteractable(BlockPos blockPos)
    {
        return isInteractable(MinecraftClient.getInstance().world.getBlockState(blockPos));
    }

    public boolean isInteractable(BlockState state)
    {
        return isInteractable(state.getBlock());
    }

    public boolean isInteractable(Block block)
    {
        return INTERACTABLE_BLOCKS.contains(block);
    }

    public boolean isExplosionResistant(BlockPos blockPos)
    {
        return isExplosionResistant(MinecraftClient.getInstance().world.getBlockState(blockPos));
    }

    public boolean isExplosionResistant(BlockState state)
    {
        return EXPLOSION_RESISTANT.contains(state.getBlock());
    }

    public boolean isUnbreakable(BlockPos blockPos)
    {
        return isUnbreakable(MinecraftClient.getInstance().world.getBlockState(blockPos));
    }

    public boolean isUnbreakable(BlockState state)
    {
        return UNBREAKABLE.contains(state.getBlock());
    }

    public Box getBoundingBox(List<BlockPos> positions, double height)
    {
        final Box box1 = new Box(positions.getFirst());
        double minX = box1.minX;
        double minY = box1.minY;
        double minZ = box1.minZ;
        double maxX = box1.maxX;
        double maxZ = box1.maxZ;
        for (BlockPos blockPos : positions)
        {
            Box box = new Box(blockPos);

            if (box.minX < minX)
            {
                minX = box.minX;
            }
            if (box.minZ < minZ)
            {
                minZ = box.minZ;
            }
            if (box.maxX > maxX)
            {
                maxX = box.maxX;
            }
            if (box.maxZ > maxZ)
            {
                maxZ = box.maxZ;
            }
        }

        return new Box(minX, minY, minZ, maxX, minY + height, maxZ);
    }
}

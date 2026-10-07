package net.shoreline.client.impl.module.impl;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.inventory.InventoryUtil;

@Getter
public class ObsidianPlacerModule extends PlacerModule
{
    private Block currentObbyBlock = Blocks.OBSIDIAN;

    public ObsidianPlacerModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public ObsidianPlacerModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected void placeObby(BlockPos placePos)
    {
        placeBlock(placePos, currentObbyBlock);
    }

    protected boolean runSingleObbyPlacement(BlockPos placePos)
    {
        int obbySlot = findBestObbySlot();
        return runSingleBlockPlacement(placePos, currentObbyBlock, obbySlot);
    }

    protected int findBestObbySlot()
    {
        int slot = InventoryUtil.getHotbarItem(Items.OBSIDIAN).getSlot();
        if (slot == -1)
        {
            currentObbyBlock = Blocks.ENDER_CHEST;
            return InventoryUtil.getHotbarItem(Items.ENDER_CHEST).getSlot();
        }

        currentObbyBlock = Blocks.OBSIDIAN;
        return slot;
    }
}

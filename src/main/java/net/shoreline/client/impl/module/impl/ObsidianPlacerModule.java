package net.shoreline.client.impl.module.impl;

import net.minecraft.item.Items;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.manager.inventory.InventoryUtil;

public class ObsidianPlacerModule extends PlacerModule
{
    public ObsidianPlacerModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public ObsidianPlacerModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public int findBestObbySlot()
    {
        int slot = InventoryUtil.getInventorySlot(Items.OBSIDIAN, anticheat.getSwapType());
        if (slot == -1)
        {
            return InventoryUtil.getInventorySlot(Items.ENDER_CHEST, anticheat.getSwapType());
        }
        return slot;
    }
}

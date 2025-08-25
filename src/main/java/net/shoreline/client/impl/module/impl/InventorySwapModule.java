package net.shoreline.client.impl.module.impl;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.inventory.InventoryUtil;

public class InventorySwapModule extends Toggleable
{
    public InventorySwapModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public InventorySwapModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected void swapItemWithSlot(Item item, int slot)
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; ++i)
        {
            ItemStack stack = playerInventory.getStack(i);
            if (!stack.getItem().equals(item))
            {
                continue;
            }

            int slot1 = InventoryUtil.getPacketSlotIndex(slot);
            Managers.INVENTORY.clickSwap(i, slot1, item);
            return;
        }
    }
}

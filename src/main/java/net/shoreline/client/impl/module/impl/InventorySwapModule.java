package net.shoreline.client.impl.module.impl;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
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

    protected int swapItemWithSlot(Item item, int slot, boolean altSwap)
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        ScreenHandler handler = mc.player.playerScreenHandler;
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; ++i)
        {
            ItemStack stack = playerInventory.getStack(i);
            if (!stack.getItem().equals(item))
            {
                continue;
            }

            int slot1 = InventoryUtil.getPacketSlotIndex(handler, slot);
            if (altSwap)
            {
                Managers.INVENTORY.swap(i, slot1);
            } else
            {
                Managers.INVENTORY.clickSwap(i, slot1, item);
            }

            return i;
        }

        return -1;
    }

    protected boolean canSwapInventory()
    {
        return mc.currentScreen == null || mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof ChatScreen;
    }
}

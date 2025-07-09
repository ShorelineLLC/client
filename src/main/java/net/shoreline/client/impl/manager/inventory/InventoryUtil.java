package net.shoreline.client.impl.manager.inventory;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@UtilityClass
public class InventoryUtil
{
    public boolean isInInventoryScreen()
    {
        return MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen
                || MinecraftClient.getInstance().currentScreen instanceof ShulkerBoxScreen
                || MinecraftClient.getInstance().currentScreen instanceof InventoryScreen;
    }

    public int getInventorySlot(Item item, SilentSwapType swapType)
    {
        return swapType == SilentSwapType.INVENTORY ? getInventorySlot(item) : getHotbarSlot(item);
    }

    public int getInventorySlot(Item item)
    {
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < inventory.getMainStacks().size(); i++)
        {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().equals(item))
            {
                return i;
            }
        }

        return -1;
    }

    public int getHotbarSlot(Item item)
    {
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++)
        {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (stack.getItem().equals(item))
            {
                return i;
            }
        }

        return -1;
    }
}

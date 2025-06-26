package net.shoreline.client.impl.player.inventory;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@UtilityClass
public class InventoryUtil
{
    public int getInventorySlot(Item item, SilentSwapType swapType)
    {
        return swapType == SilentSwapType.INVENTORY ? getInventorySlot(item) : getHotbarSlot(item);
    }

    public int getInventorySlot(Item item)
    {
        for (int i = 0; i < 36; i++)
        {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (stack.getItem().equals(item))
            {
                return i < 9 ? i + 36 : i;
            }
        }

        return -1;
    }

    public int getHotbarSlot(Item item)
    {
        for (int i = 0; i < 9; i++)
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

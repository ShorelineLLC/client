package net.shoreline.client.impl.manager.inventory;

import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Getter
public class HotbarCache
{
    private final ItemStack[] hotbarItems;

    public HotbarCache(PlayerInventory playerInventory)
    {
        this.hotbarItems = new ItemStack[PlayerInventory.getHotbarSize()];
        for (int i = 0; i < hotbarItems.length; i++)
        {
            ItemStack stack = playerInventory.getStack(i);
            hotbarItems[i] = stack;
        }
    }

    public ItemStack getStack(int slot)
    {
        return hotbarItems[slot];
    }
}

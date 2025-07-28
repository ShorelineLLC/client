package net.shoreline.client.impl.inventory;

import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Getter
public class HotbarCache
{
    private final ItemStack[] hotbarItems;

    public HotbarCache(PlayerInventory playerInventory)
    {
        this(playerInventory, true);
    }

    public HotbarCache(PlayerInventory playerInventory, boolean allowEmpty)
    {
        this.hotbarItems = new ItemStack[PlayerInventory.getHotbarSize()];
        for (int i = 0; i < hotbarItems.length; i++)
        {
            ItemStack stack = playerInventory.getStack(i);
            if (!allowEmpty && stack.isEmpty())
            {
                continue;
            }
            hotbarItems[i] = stack.copy();
        }
    }

    public ItemStack getStack(int slot)
    {
        return hotbarItems[slot];
    }
}

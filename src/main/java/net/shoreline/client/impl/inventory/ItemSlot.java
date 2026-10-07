package net.shoreline.client.impl.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
@Getter
@Setter
public class ItemSlot
{
    private final int slot;
    private final ItemStack itemStack;

    public ItemSlot(PlayerInventory inventory, int slot)
    {
        this.slot = slot;
        if (slot == InventoryUtil.INVALID_SLOT)
        {
            this.itemStack = ItemStack.EMPTY;
            return;
        }

        this.itemStack = inventory.getStack(slot);
    }

    public Item getItem()
    {
        return itemStack.getItem();
    }
}

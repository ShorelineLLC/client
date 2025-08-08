package net.shoreline.client.impl.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
@Getter
@Setter
public class ItemSlot
{
    private final int slot;
    private final ItemStack itemStack;

    public Item getItem()
    {
        return itemStack.getItem();
    }
}

package net.shoreline.client.impl.event.network;

import net.minecraft.item.ItemStack;
import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

@Cancelable
public class ItemDesyncEvent extends Event {

    private ItemStack stack;



    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getServerItem() {
        return stack;
    }
}

package net.shoreline.client.impl.event.network;

import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;

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

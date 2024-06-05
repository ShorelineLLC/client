package net.shoreline.client.impl.event.item;

import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.event.Event;

public class FinishUsingEvent extends Event
{
    private final ItemStack stack;

    public FinishUsingEvent(ItemStack stack)
    {
        this.stack = stack;
    }

    public ItemStack getStack()
    {
        return stack;
    }
}

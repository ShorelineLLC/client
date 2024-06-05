package net.shoreline.client.impl.event.entity.player;

import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.event.Event;

public class SetStackEvent extends Event
{

    private final int slot;
    private final ItemStack stack;

    public SetStackEvent(int slot, ItemStack stack)
    {
        this.slot = slot;
        this.stack = stack;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public int getSlot()
    {
        return slot;
    }
}

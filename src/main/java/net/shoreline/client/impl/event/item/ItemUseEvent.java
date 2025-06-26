package net.shoreline.client.impl.event.item;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class ItemUseEvent extends Event
{
    private ItemStack itemStack;
}

package net.shoreline.client.impl.event.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
public class ItemUseEvent extends Event
{
    private final Item item;

    @Cancelable
    @Getter
    @Setter
    public static class Block extends Event
    {
        private ItemStack itemStack;
    }
}

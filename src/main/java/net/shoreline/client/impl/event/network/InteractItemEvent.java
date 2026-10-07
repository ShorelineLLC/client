package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class InteractItemEvent extends Event
{
    private final ItemStack itemStack;

    public Item getItem()
    {
        return itemStack.getItem();
    }

    public static class Pre extends InteractItemEvent {
        public Pre(ItemStack item) {
            super(item);
        }
    }

    public static class Post extends InteractItemEvent {
        public Post(ItemStack item) {
            super(item);
        }
    }
}

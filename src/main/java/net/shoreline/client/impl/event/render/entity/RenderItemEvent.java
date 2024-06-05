package net.shoreline.client.impl.event.render.entity;

import net.minecraft.entity.ItemEntity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class RenderItemEvent extends Event
{
    private final ItemEntity itemEntity;

    public RenderItemEvent(ItemEntity itemEntity)
    {
        this.itemEntity = itemEntity;
    }

    public ItemEntity getItem()
    {
        return itemEntity;
    }
}

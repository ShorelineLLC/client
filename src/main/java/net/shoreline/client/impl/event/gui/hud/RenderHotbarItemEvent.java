package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Setter
@Getter
@Cancelable
public class RenderHotbarItemEvent extends Event
{
    private final int seed;

    private ItemStack stack;

    public RenderHotbarItemEvent(int seed, ItemStack stack)
    {
        this(seed);
        this.stack = stack;
    }
}

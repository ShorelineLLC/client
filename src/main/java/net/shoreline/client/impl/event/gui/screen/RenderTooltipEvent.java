package net.shoreline.client.impl.event.gui.screen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@RequiredArgsConstructor
public class RenderTooltipEvent extends Event
{
    private final DrawContext context;
    private final ItemStack stack;
    private final int x, y;
}

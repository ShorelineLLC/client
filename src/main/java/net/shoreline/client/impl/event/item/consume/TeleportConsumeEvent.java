package net.shoreline.client.impl.event.item.consume;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
@Cancelable
public class TeleportConsumeEvent extends Event
{
    private final ItemStack stack;
}

package net.shoreline.client.impl.event;

import net.shoreline.client.mixin.MixinMinecraftClient;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * Allows mining and eating at the same time
 *
 * @see MixinMinecraftClient
 */
@Cancelable
public class ItemMultitaskEvent extends Event
{

}

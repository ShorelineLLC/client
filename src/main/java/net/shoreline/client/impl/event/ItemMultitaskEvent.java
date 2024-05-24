package net.shoreline.client.impl.event;

import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;
import net.shoreline.client.mixin.MixinMinecraftClient;

/**
 * Allows mining and eating at the same time
 *
 * @see MixinMinecraftClient
 */
@Cancelable
public class ItemMultitaskEvent extends Event {

}

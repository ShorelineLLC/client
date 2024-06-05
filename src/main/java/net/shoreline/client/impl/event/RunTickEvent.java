package net.shoreline.client.impl.event;

import net.shoreline.client.mixin.MixinMinecraftClient;
import net.shoreline.eventbus.event.Event;

/**
 * The main game loop event, this "tick" runs while the
 * {@link net.minecraft.client.MinecraftClient#running} var is <tt>true</tt>.
 *
 * @author linus
 * @see MixinMinecraftClient
 * @since 1.0
 */
public class RunTickEvent extends Event
{

}

package net.shoreline.client.impl.event.network;

import net.minecraft.client.input.Input;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
public class MovementSlowdownEvent extends Event
{
    //
    public final Input input;

    /**
     * @param input
     */
    public MovementSlowdownEvent(Input input)
    {
        this.input = input;
    }

    /**
     * @return
     */
    public Input getInput()
    {
        return input;
    }
}

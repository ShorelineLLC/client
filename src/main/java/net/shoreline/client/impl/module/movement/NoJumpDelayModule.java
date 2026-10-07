package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.entity.JumpDelayEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoJumpDelayModule extends Toggleable
{
    public NoJumpDelayModule()
    {
        super("NoJumpDelay", "Removes the vanilla jump delay", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onJumpDelay(JumpDelayEvent event)
    {
        event.cancel();
    }
}

package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.JumpDelayEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoJumpDelayModule extends ToggleModule
{
    public NoJumpDelayModule()
    {
        super("NoJumpDelay", "Removes the vanilla jump delay", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onJumpDelay(JumpDelayEvent event)
    {
        event.cancel();
    }
}

package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.InteractBorderEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class BorderInteractModule extends ToggleModule
{
    public BorderInteractModule()
    {
        super("BorderInteract", "Allows you to interact with the world border", ModuleCategory.WORLD);
    }

    @EventListener
    public void onInteractBorder(InteractBorderEvent event)
    {
        event.cancel();
    }
}

package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.screen.slot.ShulkerNestedEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class ShulkerceptionModule extends ToggleModule
{
    public ShulkerceptionModule()
    {
        super("Shulkerception", "Allows you to put shulkers in shulkers", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onShulkerNested(ShulkerNestedEvent event)
    {
        event.cancel();
    }
}

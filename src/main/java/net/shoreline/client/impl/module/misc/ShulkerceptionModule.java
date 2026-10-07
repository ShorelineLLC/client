package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.gui.screen.slot.ShulkerNestedEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class ShulkerceptionModule extends Toggleable
{
    public ShulkerceptionModule()
    {
        super("Shulkerception", "Put shulkers inside shulkers", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onShulkerNested(ShulkerNestedEvent event)
    {
        event.cancel();
    }
}

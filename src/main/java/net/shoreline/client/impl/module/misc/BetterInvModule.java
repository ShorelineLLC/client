package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.screen.MouseDraggedEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class BetterInvModule extends ToggleModule {

    Config<Boolean> dragItemMoveConfig = register(new BooleanConfig("DragItemMove", "Allows you to hold down shift and drag move items.", true));

    public BetterInvModule()
    {
        super("BetterInv", "Makes your inventory better", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void mouseDragged(MouseDraggedEvent event)
    {
        if (dragItemMoveConfig.getValue())
        {
            event.cancel();
        }
    }
}

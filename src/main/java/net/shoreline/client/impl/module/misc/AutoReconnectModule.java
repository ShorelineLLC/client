package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

/**
 * @see net.shoreline.client.mixin.gui.screen.MixinDisconnectedScreen
 */
public class AutoReconnectModule extends Toggleable
{
    public static AutoReconnectModule INSTANCE;

    public AutoReconnectModule()
    {
        super("AutoReconnect", "Reconnects automatically after disconnect", GuiCategory.MISCELLANEOUS);
        INSTANCE = this;
    }
}

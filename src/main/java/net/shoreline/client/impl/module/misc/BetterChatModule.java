package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class BetterChatModule extends Toggleable
{
    public BetterChatModule()
    {
        super("BetterChat", "Improves in-game chat", GuiCategory.MISCELLANEOUS);
    }
}

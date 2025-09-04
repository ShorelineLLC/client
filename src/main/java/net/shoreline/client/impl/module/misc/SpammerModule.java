package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class SpammerModule extends Toggleable
{

    public SpammerModule()
    {
        super("Spammer", "Spams in chat", GuiCategory.MISCELLANEOUS);
    }
}

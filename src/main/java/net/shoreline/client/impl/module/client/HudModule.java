package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class HudModule extends Toggleable
{

    public HudModule() {
        super("HUD", "Heads up display", GuiCategory.CLIENT);
    }
}

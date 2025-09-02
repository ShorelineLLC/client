package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class StepModule extends Toggleable
{
    public StepModule()
    {
        super("Step", "Step up blocks", GuiCategory.MOVEMENT);
    }
}

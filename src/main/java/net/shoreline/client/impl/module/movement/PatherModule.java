package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class PatherModule extends Toggleable
{

    public PatherModule()
    {
        super("Pather", new String[] {"AutoWalk"}, "Moves forward while avoiding obstacles", GuiCategory.MOVEMENT);
    }
}

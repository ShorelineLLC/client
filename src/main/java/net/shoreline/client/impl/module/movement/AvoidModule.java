package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AvoidModule extends Toggleable
{

    public AvoidModule()
    {
        super("Avoid", "Prevents collisions with harmful blocks", GuiCategory.MOVEMENT);
    }


}

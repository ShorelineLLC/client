package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ElytraFlyModule extends Toggleable
{

    public ElytraFlyModule()
    {
        super("ElytraFly", "Fly while wearing elytra", GuiCategory.MOVEMENT);
    }
}

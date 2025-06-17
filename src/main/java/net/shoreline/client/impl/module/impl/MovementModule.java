package net.shoreline.client.impl.module.impl;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class MovementModule extends Toggleable
{
    public MovementModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public MovementModule(final String name,
                          final String[] nameAliases,
                          final String description,
                          final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected boolean isInputtingMovement()
    {
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
    }
}

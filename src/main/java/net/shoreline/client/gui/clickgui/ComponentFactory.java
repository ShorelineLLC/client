package net.shoreline.client.gui.clickgui;

import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;

public class ComponentFactory
{
    public ModuleComponent createModuleComponent(Module module,
                                                 Frame frame,
                                                 int x,
                                                 int y,
                                                 int width,
                                                 int height)
    {
        if (module instanceof Toggleable toggleable)
        {
            return new ToggleComponent(toggleable, frame, x, y, width, height);
        }

        return new ModuleComponent(module, frame, x, y, width, height);
    }
}

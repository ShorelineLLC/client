package net.shoreline.client.gui.clickgui;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.config.CheckboxComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;

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

    public ConfigComponent<?> createConfigComponent(Config<?> config,
                                                    ModuleComponent moduleComponent,
                                                    Frame frame,
                                                    int x,
                                                    int y,
                                                    int width,
                                                    int height)
    {
        if (config.getValue() instanceof Boolean)
        {
            return new CheckboxComponent((Config<Boolean>) config, moduleComponent, frame, x, y, width, height);
        }

        return null;
    }
}

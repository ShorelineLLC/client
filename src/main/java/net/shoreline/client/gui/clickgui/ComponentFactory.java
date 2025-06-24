package net.shoreline.client.gui.clickgui;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.config.*;

import java.awt.*;

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
        if (config.getValue() instanceof Macro)
        {
            return new KeyListenerComponent((Config<Macro>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof Boolean)
        {
            return new CheckboxComponent((Config<Boolean>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof Double)
        {
            return new SliderComponent<>((Config<Double>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof Float)
        {
            return new SliderComponent<>((Config<Float>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof Integer)
        {
            return new SliderComponent<>((Config<Integer>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof Enum<?>)
        {
            return new SelectorComponent((Config<Enum<?>>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof Color)
        {
            return new ColorPickerComponent((Config<Color>) config, moduleComponent, frame, x, y, width, height);
        }

        if (config.getValue() instanceof String)
        {
            return new TextboxComponent((Config<String>) config, moduleComponent, frame, x, y, width, height);
        }

        throw new IllegalArgumentException("No component exists for the config type!");
    }
}

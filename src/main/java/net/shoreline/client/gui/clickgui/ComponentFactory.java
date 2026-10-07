package net.shoreline.client.gui.clickgui;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.ToggleableConfigGroup;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.config.*;
import net.shoreline.client.gui.clickgui.config.picker.ColorPickerComponent;
import net.shoreline.client.gui.clickgui.config.picker.GroupComponent;
import net.shoreline.client.gui.clickgui.config.picker.RegistryPickerComponent;
import net.shoreline.client.gui.clickgui.config.picker.ToggleableGroupComponent;

import java.awt.*;
import java.util.Collection;

public class ComponentFactory
{
    public ModuleComponent createModuleComponent(Module module,
                                                 Frame frame,
                                                 float x,
                                                 float y,
                                                 float width,
                                                 float height)
    {
        if (module instanceof Toggleable toggleable)
        {
            return new ToggleModuleComponent(toggleable, frame, x, y, width, height);
        }

        return new ModuleComponent(module, frame, x, y, width, height);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ConfigComponent<?> createConfigComponent(Config<?> config,
                                                    ModuleComponent moduleComponent,
                                                    Frame frame,
                                                    float x,
                                                    float y,
                                                    float width,
                                                    float height)
    {
        if (!config.getChildren().isEmpty())
        {
            if (config.getValue() instanceof Boolean)
            {
                ToggleableGroupComponent group = new ToggleableGroupComponent((Config<Boolean>) config, moduleComponent, frame, x, y, width, height);
                for (Config<?> cfg : (ToggleableConfigGroup) config)
                {
                    ConfigComponent<?> component = createConfigComponent(cfg, moduleComponent, frame, x  + 2, y, width - 2, height);
                    group.getComponents().add(component);
                    frame.getAllComponents().add(component);
                }

                return group;
            }
            else
            {
                GroupComponent groupComponent = new GroupComponent((Config<Void>) config, moduleComponent, frame, x, y, width, height);
                for (Config<?> cfg : (ConfigGroup) config)
                {
                    ConfigComponent<?> component = createConfigComponent(cfg, moduleComponent, frame, x + 2, y, width - 2, height);
                    groupComponent.getComponents().add(component);
                    frame.getAllComponents().add(component);
                }

                return groupComponent;
            }
        } else
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

            if (config.getValue() instanceof Collection<?>)
            {
                Config<Collection<Object>> colCfg = (Config) config;
                return new RegistryPickerComponent<>(colCfg, moduleComponent, frame, x, y, width, height);
            }
        }

        throw new IllegalArgumentException("No component exists for the config type: " + config.getClass().getCanonicalName());
    }
}

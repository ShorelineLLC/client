package net.shoreline.client.api.config;

import net.shoreline.client.api.macro.Macro;

import java.awt.*;
import java.util.Collection;

public class ConfigFactory<T>
{
    private final T defaultValue;

    public ConfigFactory(T defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Config<T> create(final String name, final String description)
    {
        if (defaultValue == null)
        {
            return (Config<T>) new ConfigGroup(name, description);
        }

        if (defaultValue instanceof Boolean)
        {
            return (Config<T>) new BooleanConfig(name, description);
        } else if (defaultValue instanceof Double)
        {
            return (Config<T>) new NumberConfig<Double>(name, description);
        } else if (defaultValue instanceof Float)
        {
            return (Config<T>) new NumberConfig<Float>(name, description);
        } else if (defaultValue instanceof Integer)
        {
            return (Config<T>) new NumberConfig<Integer>(name, description);
        } else if (defaultValue instanceof Enum<?>)
        {
            return (Config<T>) new EnumConfig<>(name, description);
        } else if (defaultValue instanceof Color)
        {
            return (Config<T>) new ColorConfig(name, description);
        } else if (defaultValue instanceof String)
        {
            return (Config<T>) new StringConfig(name, description);
        } else if (defaultValue instanceof Macro)
        {
            return (Config<T>) new MacroConfig(name, description);
        } else if (defaultValue instanceof Collection<?>)
        {
            return (Config<T>) new RegistryConfig<>(name, description);
        }

        throw new IllegalArgumentException("Unsupported config type: " + defaultValue.getClass().getName());
    }
}

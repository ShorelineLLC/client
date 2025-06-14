package net.shoreline.client.api.config;

import java.util.function.Supplier;

public abstract class ConfigBuilder<T>
{
    private ConfigFactory<T> factory;

    private final String name;
    private final String description;

    private String[] nameAliases;
    private T defaultValue;
    private Supplier<Boolean> visible;

    public ConfigBuilder(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public ConfigBuilder<T> setNameAliases(String... aliases)
    {
        this.nameAliases = aliases;
        return this;
    }

    public ConfigBuilder<T> setDefaultValue(T value)
    {
        this.defaultValue = value;
        this.factory = new ConfigFactory<>(value);
        return this;
    }

    public ConfigBuilder<T> setVisible(Supplier<Boolean> visible)
    {
        this.visible = visible;
        return this;
    }

    public Config<T> build()
    {
        if (factory == null)
        {
            throw new IllegalStateException("Config has no default value!");
        }

        final Config<T> build = factory.create(name, description);
        if (nameAliases != null)
        {
            build.setNameAliases(nameAliases);
        }

        if (defaultValue != null)
        {
            build.setValue(defaultValue);
        }

        build.setVisible(visible);
        return build;
    }
}

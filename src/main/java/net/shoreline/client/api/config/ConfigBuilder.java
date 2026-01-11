package net.shoreline.client.api.config;

import java.util.function.Supplier;

public abstract class ConfigBuilder<T>
{
    private ConfigFactory<T> factory;

    protected final String name;

    protected String description;
    private String[] nameAliases;
    private T defaultValue;
    private Supplier<Boolean> visible;

    public ConfigBuilder(String name)
    {
        this.name = name;
        this.description = "No description found!";
    }

    public ConfigBuilder<T> setDescription(String description)
    {
        this.description = description;
        return this;
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
        return buildWithoutFactory(factory.create(name, description));
    }

    public Config<T> buildWithoutFactory(Config<T> config)
    {
        if (factory == null)
        {
            throw new IllegalStateException("Config has no default value!");
        }

        if (nameAliases != null)
        {
            config.setNameAliases(nameAliases);
        }

        if (defaultValue != null)
        {
            config.setValue(defaultValue);
            config.setDefaultValue(defaultValue);
        }

        config.setVisible(visible);
        return config;
    }
}

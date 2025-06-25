package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.Serializable;

import java.util.function.Supplier;

@Getter
@Setter
public abstract class Config<T> implements Identifiable, Serializable
{
    private final String name;
    private final String description;

    private String[] nameAliases;
    private ConfigGroup configGroup;

    private T value;
    private final T defaultValue;

    private Supplier<Boolean> visible;

    public Config(String name, String description)
    {
        this.name = name;
        this.description = description;
        this.defaultValue = value;
    }

    @Override
    public JsonObject toJson()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("id", getId());
        return jsonObject;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String[] getAliases()
    {
        return nameAliases;
    }

    @Override
    public String getId()
    {
        return String.format("%s_config", name.toLowerCase());
    }

    public boolean isVisible()
    {
        return visible != null ? visible.get() : true;
    }

    public boolean isGroup()
    {
        return false;
    }

    public boolean isGrouped()
    {
        return configGroup != null;
    }
}

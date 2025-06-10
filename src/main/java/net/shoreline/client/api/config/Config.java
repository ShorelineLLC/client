package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.Serializable;

import java.util.function.Supplier;

public class Config<T> implements Identifiable, Serializable
{
    private final String name;

    @Getter
    @Setter
    private String[] nameAliases;

    @Getter
    private final String description;

    @Getter
    @Setter
    private T value;

    @Getter
    private final T defaultValue;

    @Setter
    private Supplier<Boolean> visible;

    public Config(String name, String description)
    {
        this.name = name;
        this.description = description;
        this.defaultValue = value;
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

    @Override
    public JsonObject toJson()
    {
        return null;
    }

    @Override
    public void fromJson(JsonObject jsonObject)
    {

    }

    public boolean isVisible()
    {
        return visible != null ? visible.get() : true;
    }
}

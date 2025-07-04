package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.Observable;
import net.shoreline.client.api.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public abstract class Config<T> implements Identifiable, Observable<T>, Serializable
{
    private final String name;
    private final String description;

    private String[] nameAliases;
    private ConfigGroup configGroup;

    private T value;
    private final T defaultValue;

    private Supplier<Boolean> visible;

    private final List<Consumer<T>> listeners = new ArrayList<>();

    public Config(String name, String description)
    {
        this.name = name;
        this.description = description;
        this.defaultValue = value;
    }

    @Override
    public void setValue(T value)
    {
        this.value = value;
        listeners.forEach(l -> l.accept(value));
    }

    @Override
    public T getValue()
    {
        return value;
    }

    @Override
    public void addListener(Consumer<T> l)
    {
        listeners.add(l);
    }

    @Override
    public void removeListener(Consumer<T> l)
    {
        listeners.remove(l);
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

package net.shoreline.client.api.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.*;

@Getter
@Setter
public class RegistryConfig<T> extends Config<Collection<T>>
{
    private Registry<T> registry;

    public RegistryConfig(String name, String description)
    {
        super(name, description);
    }

    public void add(T element)
    {
        Collection<T> val = new LinkedHashSet<>(getValue());
        val.add(element);
        setValue(val);
    }

    public void remove(T element)
    {
        Collection<T> val = new LinkedHashSet<>(getValue());
        val.remove(element);
        setValue(val);
    }

    public void clear()
    {
        Collection<T> val = new LinkedHashSet<>();
        setValue(val);
    }

    public boolean contains(T element)
    {
        return getValue().contains(element);
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = super.toJson();

        JsonArray array = new JsonArray();
        for (T registryKey : getValue())
        {
            Identifier id = registry.getId(registryKey);
            if (id == null)
            {
                continue;
            }

            array.add(id.toString());
        }

        jsonObject.add("value", array);
        return jsonObject;
    }

    public static class Builder<T> extends ConfigBuilder<Collection<T>>
    {
        private Collection<T> values;
        private Registry<T> registry;

        public Builder(String name)
        {
            super(name);
            setDefaultValue(new LinkedHashSet<>());
        }

        public Builder<T> setRegistry(Registry<T> registry)
        {
            this.registry = registry;
            return this;
        }

        @SafeVarargs
        public final Builder<T> setValues(T... values)
        {
            this.values = Set.of(values);
            return this;
        }

        @Override
        public RegistryConfig<T> build()
        {
            RegistryConfig<T> config = (RegistryConfig<T>) super.build();
            config.setRegistry(registry);
            config.getValue().addAll(values);
            return config;
        }
    }
}

package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Setter
public class ListConfig<E, T extends List<E>> extends Config<T>
{
    private Function<E, String> mapping;

    public ListConfig(String name, String description)
    {
        super(name, description);
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = super.toJson();
        StringBuilder builder = new StringBuilder();
        for (E value : getValue())
        {
            builder.append(mapping.apply(value)).append("-");
        }

        jsonObject.addProperty("value", builder.toString());
        return jsonObject;
    }

    public void add(E value)
    {
        getValue().add(value);
    }

    @SafeVarargs
    public final void addAll(E... values)
    {
        for (E value : values)
        {
            add(value);
        }
    }

    public boolean contains(Object object)
    {
        return getValue().contains((E) object);
    }

    public static class Builder<E, T extends List<E>> extends ConfigBuilder<E>
    {
        private final List<E> values = new ArrayList<>();
        private Function<E, String> idMapping = Object::toString;

        public Builder(String name)
        {
            super(name);
        }

        public Builder<E, T> add(E value)
        {
            values.add(value);
            return this;
        }

        @SafeVarargs
        public final Builder<E, T> addAll(E... values)
        {
            for (E value : values)
            {
                add(value);
            }

            return this;
        }

        public Builder<E, T> setMapping(Function<E, String> idMapping)
        {
            this.idMapping = idMapping;
            return this;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public ListConfig build()
        {
            ListConfig config = (ListConfig) super.build();
            config.setValue(values);
            config.setMapping(idMapping);
            return config;
        }
    }
}

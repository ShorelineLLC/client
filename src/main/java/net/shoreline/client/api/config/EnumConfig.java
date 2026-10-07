package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.impl.file.ModConfiguration;

@Getter
@Setter
public class EnumConfig<T extends Enum<?>> extends Config<T>
{
    private T[] values;
    private int index;

    public EnumConfig(String name, String description)
    {
        super(name, description);
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("value", getValue().name());
        return jsonObject;
    }

    @Override
    public void setValue(T value)
    {
        super.setValue(value);
        this.index = value.ordinal();
    }

    public static class Builder<T extends Enum<?>> extends ConfigBuilder<T>
    {
        private T[] values;

        public Builder(String name) {
            super(name);
        }

        public ConfigBuilder<T> setValues(T[] values)
        {
            this.values = values;
            return this;
        }

        @Override
        public EnumConfig<T> build()
        {
            final EnumConfig<T> build = (EnumConfig<T>) super.build();
            build.setValues(values);
            return build;
        }
    }
}

package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

public class EnumConfig<T extends Enum<?>> extends Config<T>
{
    @Getter
    @Setter
    private T[] values;

    @Getter
    private int index;

    public EnumConfig(String name, String description)
    {
        super(name, description);
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

    public static class Builder<T extends Enum<?>> extends ConfigBuilder<T>
    {
        private T[] values;

        public Builder(String name, String description) {
            super(name, description);
        }

        public Builder<T> setValues(T[] values)
        {
            this.values = values;
            return this;
        }

        @Override
        public Config<T> build()
        {
            final EnumConfig<T> build = (EnumConfig<T>) super.build();
            build.setValues(values);
            return build;
        }
    }
}

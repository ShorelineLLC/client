package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NumberConfig<T extends Number> extends Config<T>
{
    private T min, max;
    private NumberFormat format;
    private int roundingPlaces;

    public NumberConfig(String name, String description) {
        super(name, description);
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("value", getValue());
        return jsonObject;
    }

    public static class Builder<T extends Number> extends ConfigBuilder<T>
    {
        private T min, max;
        private NumberFormat format;
        private int roundingScale;

        public Builder(String name) {
            super(name);
        }

        @Override
        public Builder<T> setDefaultValue(T defaultValue)
        {
            super.setDefaultValue(defaultValue);
            this.roundingScale = defaultValue instanceof Float || defaultValue instanceof Double ? 1 : 0;
            return this;
        }

        public Builder<T> setMin(T min)
        {
            this.min = min;
            return this;
        }

        public Builder<T> setMax(T max)
        {
            this.max = max;
            return this;
        }

        public Builder<T> setFormat(NumberFormat format)
        {
            this.format = format;
            return this;
        }

        public Builder<T> setRoundingScale(int roundingScale)
        {
            this.roundingScale = roundingScale;
            return this;
        }

        @Override
        public Config<T> build()
        {
            final NumberConfig build = (NumberConfig) super.build();
            if (min != null)
            {
                build.setMin((Number) min);
            }
            if (max != null)
            {
                build.setMax((Number) max);
            }

            build.setFormat(format);
            build.setRoundingPlaces(roundingScale);
            return build;
        }
    }
}

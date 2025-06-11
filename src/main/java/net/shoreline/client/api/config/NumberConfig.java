package net.shoreline.client.api.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NumberConfig<T extends Number> extends Config<T>
{
    private T min, max;

    private int roundingPlaces;

    public NumberConfig(String name, String description) {
        super(name, description);
    }

    public static class Builder<T extends Number> extends ConfigBuilder<T>
    {
        private T min, max;
        private int roundingScale = 0;

        public Builder(String name, String description) {
            super(name, description);
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

            build.setRoundingPlaces(roundingScale);
            return build;
        }
    }
}

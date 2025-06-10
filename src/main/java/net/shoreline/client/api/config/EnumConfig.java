package net.shoreline.client.api.config;

import lombok.Getter;
import lombok.Setter;

public class EnumConfig<T extends Enum<?>> extends Config<T>
{
    @Getter
    @Setter
    private T[] values;

    @Getter
    private int index;

    public EnumConfig(String name, String description, T[] values)
    {
        super(name, description);
        this.values = values;
    }

    public static class Builder<T extends Enum<?>> extends ConfigBuilder<T>
    {
        public Builder(String name, String description) {
            super(name, description);
        }
    }
}

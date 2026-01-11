package net.shoreline.client.api.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

@Getter
@Setter
public class ToggleableConfigGroup extends BooleanConfig implements Iterable<Config<?>>
{
    private LinkedHashMap<String, Config<?>> configs;

    public ToggleableConfigGroup(String name, String description)
    {
        super(name, description);
    }

    @Override
    public @NotNull Iterator<Config<?>> iterator()
    {
        return configs.sequencedValues().iterator();
    }

    @Override
    public Collection<Config<?>> getChildren()
    {
        return configs.sequencedValues();
    }

    public static class Builder extends ConfigBuilder<Boolean>
    {
        private final LinkedHashMap<String, Config<?>> configs = new LinkedHashMap<>();

        public Builder(String name)
        {
            super(name);
        }

        public ToggleableConfigGroup.Builder add(Config<?> config)
        {
            configs.put(config.getName(), config);
            return this;
        }

        public ToggleableConfigGroup.Builder addAll(Config<?>... configs1)
        {
            Arrays.stream(configs1).forEach(c -> configs.put(c.getName(), c));
            return this;
        }

        public ToggleableConfigGroup build()
        {
            ToggleableConfigGroup group = (ToggleableConfigGroup) super.buildWithoutFactory(new ToggleableConfigGroup(name, description));
            group.setConfigs(configs);

            configs.values().forEach(c -> c.setConfigGroup(group));
            return group;
        }
    }
}

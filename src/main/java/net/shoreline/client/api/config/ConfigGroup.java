package net.shoreline.client.api.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

@Getter
@Setter
public class ConfigGroup extends Config<Void> implements Iterable<Config<?>>
{
    private LinkedHashMap<String, Config<?>> configs;

    public ConfigGroup(String name, String description)
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

    @Override
    public JsonObject toJson()
    {
        JsonObject json = super.toJson();
        JsonArray arr = new JsonArray();
        for (Config<?> cfg : configs.values())
        {
            arr.add(cfg.toJson());
        }
        json.add("value", arr);
        return json;
    }

    public static class Builder extends ConfigBuilder<Void>
    {
        private final LinkedHashMap<String, Config<?>> configs = new LinkedHashMap<>();

        public Builder(String name) {
            super(name);
        }

        public Builder add(Config<?> config)
        {
            configs.put(config.getName(), config);
            return this;
        }

        public Builder addAll(Config<?>... configs1)
        {
            Arrays.stream(configs1).forEach(c -> configs.put(c.getName(), c));
            return this;
        }

        public ConfigGroup build()
        {
            setDefaultValue(null);
            ConfigGroup group = (ConfigGroup) super.build();
            group.setConfigs(configs);

            configs.values().forEach(c -> c.setConfigGroup(group));
            return group;
        }
    }
}

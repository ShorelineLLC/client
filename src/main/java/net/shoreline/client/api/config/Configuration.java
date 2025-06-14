package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.Serializable;

import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public abstract class Configuration extends GenericFeature implements Serializable
{
    private final LinkedHashMap<String, Config<?>> configs = new LinkedHashMap<>();

    public Configuration(String name, String[] nameAliases) {
        super(name, nameAliases);
    }

    protected void registerConfig(Config<?> config)
    {
        configs.put(config.getId(), config);
    }

    public Config<?> getConfig(String id)
    {
        return configs.get(id);
    }

    public SequencedCollection<Config<?>> getConfigs()
    {
        return configs.sequencedValues();
    }
}

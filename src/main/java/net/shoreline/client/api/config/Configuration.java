package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.Serializable;

import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public class Configuration extends GenericFeature implements Serializable
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

    @Override
    public JsonObject toJson()
    {
        return null;
    }

    @Override
    public void fromJson(JsonObject jsonObject)
    {

    }

    public SequencedCollection<Config<?>> getConfigs()
    {
        return configs.sequencedValues();
    }
}

package net.shoreline.client.api.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.shoreline.client.api.Serializable;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.loader.Loader;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public abstract class ConfigContainer extends NetworkHandler implements Serializable
{
    private final LinkedHashMap<String, Config<?>> configs = new LinkedHashMap<>();

    public ConfigContainer(String name, String[] nameAliases) {
        super(name, nameAliases);
    }

    /** Only in the dev environment **/
    public void reflectConfigs()
    {
        for (Field field : getClass().getDeclaredFields())
        {
            if (Config.class.isAssignableFrom(field.getType()))
            {
                try
                {
                    field.setAccessible(true);
                    Config<?> config = (Config<?>) field.get(this);
                    if (config == null)
                    {
                        continue;
                    }

                    registerConfig(config);
                } catch (IllegalArgumentException | IllegalAccessException e)
                {
                    Loader.error("Failed to build config from field {}!", field.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public JsonObject toJson()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", getName());
        jsonObject.addProperty("id", getId());

        final JsonArray array = new JsonArray();
        for (Config<?> config : getConfigs())
        {
            if (config instanceof ConfigGroup || config.getName().equalsIgnoreCase("Keybind"))
            {
                continue;
            }

            array.add(config.toJson());
        }

        jsonObject.add("configs", array);
        return jsonObject;
    }

    protected void registerConfig(Config<?> config)
    {
        configs.put(config.getId(), config);
    }

    protected void registerConfigs(Config<?>... config)
    {
        Arrays.stream(config).forEach(this::registerConfig);
    }

    protected void unregisterConfig(Config<?> config)
    {
        configs.remove(config.getId());
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

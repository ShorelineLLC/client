package net.shoreline.client.api.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.config.setting.*;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.util.Globals;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Container for {@link Config} backed by a {@link ConcurrentMap}. Manages
 * all declared configurations for the container class.
 *
 * @author linus
 * @see Config
 * @since 1.0
 */
public class ConfigContainer implements Identifiable, Serializable<Config<?>>, Globals
{
    // Container name is its UNIQUE identifier.
    protected final String name;
    // List of all configurations in the container. The configs are managed
    // by a Map with references to their data tags.
    private final Map<String, Config<?>> configurations =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Declared {@link Config}s will not be registered if this process does not complete.
     *
     * @param name The container name
     */
    public ConfigContainer(String name)
    {
        // set name of this container early
        // DO NOT MOVE THIS BACK - aesthetical
        this.name = name;
    }

    /**
     * @param config
     */
    protected <T> Config<T> register(Config<T> config)
    {
        config.setContainer(this);
        configurations.put(config.getId(), config);
        return config;
    }

    /**
     * @param configs
     */
    protected void register(Config<?>... configs)
    {
        for (Config<?> config : configs)
        {
            register(config);
        }
    }

    protected void unregister(Config<?> config)
    {
        configurations.remove(config.getId());
    }

    protected void unregister(Config<?>... configs)
    {
        for (Config<?> config : configs)
        {
            unregister(config);
        }
    }

    /**
     * Reflect configuration fields
     */
//    public void reflectConfigs() {
//        final ConfigFactory factory = new ConfigFactory(this);
//        // populate container using reflection
//        for (Field field : getClass().getDeclaredFields()) {
//            if (Config.class.isAssignableFrom(field.getType())) {
//                Config<?> config = factory.build(field);
//                if (config == null) {
//                    // failsafe for debugging purposes
//                    Shoreline.error("Value for field {} is null!", field);
//                    continue;
//                }
//                register(config);
//            }
//        }
//    }

    /**
     * Returns the container as a {@link JsonObject} containing a list of the
     * registered {@link Config}
     *
     * @return The container as a json object
     * @see Config#toJson()
     */
    @Override
    public JsonObject toJson()
    {
        final JsonObject out = new JsonObject();
        out.addProperty("name", getName());
        out.addProperty("id", getId());
        final JsonArray array = new JsonArray();
        for (Config<?> config : getConfigs())
        {
            if (config.getName().equalsIgnoreCase("Keybind"))
            {
                continue;
            }
            array.add(config.toJson());
        }
        out.add("configs", array);
        return out;
    }

    /**
     * Reads the configuration values from a {@link JsonObject} and updates
     * the {@link Config} values.
     *
     * @param jsonObj The container as a json object
     * @return
     * @see Config#fromJson(JsonObject)
     */
    @Override
    public Config<?> fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("configs"))
        {
            JsonElement element = jsonObj.get("configs");
            if (!element.isJsonArray())
            {
                return null;
            }
            for (JsonElement je : element.getAsJsonArray())
            {
                if (!je.isJsonObject())
                {
                    continue;
                }
                final JsonObject configObj = je.getAsJsonObject();
                final JsonElement id = configObj.get("id");
                //
                Config<?> config = getConfig(id.getAsString());
                if (config == null)
                {
                    continue;
                }
                try
                {
                    if (config instanceof ToggleConfig cfg)
                    {
                        Boolean val = cfg.fromJson(configObj);
                        if (mc.world != null)
                        {
                            if (val)
                            {
                                cfg.disable();
                                cfg.enable();
                            }
                            else
                            {
                                cfg.disable();
                            }
                        }
                        else
                        {
                            cfg.setValue(val);
                        }
                    }
                    else if (config instanceof BooleanConfig cfg)
                    {
                        Boolean val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                    else if (config instanceof ColorConfig cfg)
                    {
                        Color val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                    else if (config instanceof EnumConfig cfg)
                    {
                        Enum<?> val = cfg.fromJson(configObj);
                        if (val != null)
                        {
                            cfg.setValue(val);
                        }
                    }
                    else if (config instanceof ItemListConfig cfg)
                    {
                        List<?> val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                    else if (config instanceof BlockListConfig cfg)
                    {
                        List<?> val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                    else if (config instanceof NumberConfig cfg)
                    {
                        Number val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                    else if (config instanceof StringConfig cfg)
                    {
                        String val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                    else if (config instanceof MacroConfig cfg)
                    {
                        Macro val = cfg.fromJson(configObj);
                        cfg.setValue(val);
                    }
                }
                // couldn't parse Json value
                catch (Exception e)
                {
                    Shoreline.error("Couldn't parse Json for {}!", config.getName());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Returns the container unique name identifier. This name will be
     * displayed to the users when representing this container.
     *
     * @return The unique name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return
     */
    @Override
    public String getId()
    {
        return String.format("%s-container", name.toLowerCase());
    }

    /**
     * Returns the {@link Config} from the reference {@link Config#getId()}
     * data tag in the registry.
     *
     * @param id The config data tag
     * @return The config from the id
     * @see Config#getId()
     */
    public Config<?> getConfig(String id)
    {
        return configurations.get(id);
    }

    /**
     * @return
     */
    public Collection<Config<?>> getConfigs()
    {
        return configurations.values();
    }
}

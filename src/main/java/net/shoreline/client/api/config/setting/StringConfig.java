package net.shoreline.client.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.config.Config;

import java.util.function.Supplier;

/**
 * @author linus
 * @since 1.0
 */
public class StringConfig extends Config<String>
{
    /**
     * Initializes the config with a default value. This constructor should
     * not be used to initialize a configuration, instead use the explicit
     * definitions of the configs in {@link net.shoreline.client.api.config.setting}.
     *
     * @param name  The unique config identifier
     * @param desc  The config description
     * @param value The default config value
     * @throws NullPointerException if value is <tt>null</tt>
     */
    public StringConfig(String name, String desc, String value)
    {
        super(name, desc, value);
    }

    public StringConfig(String name, String desc, String value, Supplier<Boolean> visible)
    {
        super(name, desc, value, visible);
    }

    /**
     * @return
     */
    @Override
    public JsonObject toJson()
    {
        JsonObject configObj = super.toJson();
        configObj.addProperty("value", getValue());
        return configObj;
    }

    /**
     * @param jsonObj
     * @return
     */
    @Override
    public String fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("value"))
        {
            JsonElement element = jsonObj.get("value");
            return element.getAsString();
        }
        return null;
    }
}

package net.shoreline.client.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.config.Config;

import java.util.function.Supplier;

/**
 * @param <T>
 * @author linus
 * @since 1.0
 */
public class EnumConfig<T extends Enum<?>> extends Config<T>
{
    // Array containing all values of the Enum type.
    private final T[] values;

    // Current Enum value index in "values" array. Used to keep track of
    // current "Mode" in dropdown menu.
    private int index;

    public EnumConfig(String name, String desc, T val, T[] values)
    {
        super(name, desc, val);
        this.values = values;
    }

    public EnumConfig(String name, String desc, T val, T[] values,
                      Supplier<Boolean> visible)
    {
        super(name, desc, val, visible);
        this.values = values;
    }

    /**
     * @return
     */
    public String getValueName()
    {
        return getValue().name();
    }

    public T[] getValues()
    {
        return values;
    }

    /**
     * Returns the next value in the {@link #values} array. If the current
     * {@link #index} is greater than the <tt>values.length</tt>, the current
     * index is wrapped to 0.
     *
     * @return The next Enum value
     */
    public T getNextValue()
    {
        index = index + 1 > values.length - 1 ? 0 : index + 1;
        return values[index];
    }

    /**
     * Returns the next value in the {@link #values} array. If the current
     * {@link #index} is greater than 0, the current index is wrapped to
     * <tt>values.length - 1</tt>.
     *
     * @return The next Enum value
     */
    public T getPreviousValue()
    {
        index = index - 1 < 0 ? values.length - 1 : index - 1;
        return values[index];
    }

    /**
     * @return
     */
    @Override
    public JsonObject toJson()
    {
        JsonObject configObj = super.toJson();
        configObj.addProperty("value", getValueName());
        return configObj;
    }

    /**
     * @param jsonObj The data as a json object
     */
    @Override
    public T fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("value"))
        {
            JsonElement element = jsonObj.get("value");

            try
            {
                return (T) (Enum<?>) Enum.valueOf((Class<Enum>) getValue().getClass(),
                        element.getAsString());
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }
        return getValue();
    }
}

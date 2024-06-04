package net.shoreline.client.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;

import java.util.function.Supplier;

/**
 * @author linus
 * @since 1.0
 */
public class NumberConfig<T extends Number> extends Config<T>
{
    // Value min and max bounds. If the current value exceeds these bounds,
    // then {@link #setValue(Number)} will clamp the value to the bounds.
    private final T min, max;
    // Number display format. Used to determine the format of the number in
    // the ClickGui when displaying the value.
    private final NumberDisplay format;
    //
    private final int roundingScale;

    /**
     * @param name
     * @param desc
     * @param min
     * @param value
     * @param max
     * @param format
     */
    public NumberConfig(String name, String desc, T min, T value, T max,
                        NumberDisplay format)
    {
        super(name, desc, value);
        this.min = min;
        this.max = max;
        this.format = format;
        // equal to number of decimal places in value
        String strValue = String.valueOf(getValue());
        this.roundingScale = strValue.substring(strValue.indexOf(".") + 1).length();
    }

    /**
     * @param name
     * @param desc
     * @param min
     * @param value
     * @param max
     * @param format
     * @param roundingScale
     */
    public NumberConfig(String name, String desc, T min, T value, T max,
                        NumberDisplay format, int roundingScale)
    {
        super(name, desc, value);
        this.min = min;
        this.max = max;
        this.format = format;
        this.roundingScale = roundingScale;
    }

    /**
     * @param name
     * @param desc
     * @param min
     * @param value
     * @param max
     * @param format
     * @param visible
     */
    public NumberConfig(String name, String desc, T min, T value, T max,
                        NumberDisplay format, Supplier<Boolean> visible)
    {
        super(name, desc, value, visible);
        this.min = min;
        this.max = max;
        this.format = format;
        String strValue = String.valueOf(getValue());
        this.roundingScale = strValue.substring(strValue.indexOf(".") + 1).length();
    }

    /**
     * @param name
     * @param desc
     * @param min
     * @param value
     * @param max
     */
    public NumberConfig(String name, String desc, T min, T value, T max)
    {
        this(name, desc, min, value, max, NumberDisplay.DEFAULT);
    }

    /**
     * @param name
     * @param desc
     * @param min
     * @param value
     * @param max
     * @param visible
     */
    public NumberConfig(String name, String desc, T min, T value, T max,
                        Supplier<Boolean> visible)
    {
        this(name, desc, min, value, max, NumberDisplay.DEFAULT, visible);

    }

    /**
     * @return
     */
    public T getMin()
    {
        return min;
    }

    /**
     * @return
     */
    public T getMax()
    {
        return max;
    }

    /**
     * @return
     */
    public boolean isMin()
    {
        return min.doubleValue() == getValue().doubleValue();
    }

    /**
     * @return
     */
    public boolean isMax()
    {
        return max.doubleValue() == getValue().doubleValue();
    }

    /**
     * @return
     */
    public int getRoundingScale()
    {
        return roundingScale;
    }

    /**
     * @return
     */
    public NumberDisplay getFormat()
    {
        return format;
    }

    /**
     * @return
     */
    public double getValueSq()
    {
        T val = getValue();
        return val.doubleValue() * val.doubleValue();
    }

    /**
     * @param val The param value
     */
    @Override
    public void setValue(T val)
    {
        // clamp
        if (val.doubleValue() < min.doubleValue())
        {
            super.setValue(min);
        }
        else if (val.doubleValue() > max.doubleValue())
        {
            super.setValue(max);
        }
        // inbounds
        else
        {
            super.setValue(val);
        }
    }

    /**
     * @return
     */
    @Override
    public JsonObject toJson()
    {
        JsonObject configObj = super.toJson();
        if (getValue() instanceof Integer)
        {
            configObj.addProperty("value", getValue());
        }
        else if (getValue() instanceof Float)
        {
            configObj.addProperty("value", getValue());
        }
        else if (getValue() instanceof Double)
        {
            configObj.addProperty("value", getValue());
        }
        return configObj;
    }

    /**
     * @param jsonObj The data as a json object
     * @return
     */
    @Override
    public T fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("value"))
        {
            JsonElement element = jsonObj.get("value");
            // get config as number
            if (getValue() instanceof Integer)
            {
                Integer val = element.getAsInt();
                return (T) val;
            }
            else if (getValue() instanceof Float)
            {
                Float val = element.getAsFloat();
                return (T) val;
            }
            else if (getValue() instanceof Double)
            {
                Double val = element.getAsDouble();
                return (T) val;
            }
        }
        return null;
    }
}

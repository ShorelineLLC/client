package net.shoreline.client.impl.command.util.impl;

import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.impl.command.util.IConfigParser;

import java.math.BigDecimal;

public class NumberConfigParser<N extends Number> implements IConfigParser<N, NumberConfig<N>>
{
    @Override
    public boolean parseString(NumberConfig<N> config, String string)
    {
        N converted = convert(config, new BigDecimal(string));
        config.setValue(converted);
        return true;
    }

    @SuppressWarnings("unchecked")
    public N convert(NumberConfig<N> config, Number number)
    {
        Class<? extends Number> type = config.getValue().getClass();
        if (type == Integer.class)
        {
            return (N) Integer.valueOf(number.intValue());
        }
        else if (type == Float.class)
        {
            return (N) Float.valueOf(number.floatValue());
        }
        else if (type == Double.class)
        {
            return (N) Double.valueOf(number.doubleValue());
        }

        throw new IllegalArgumentException("Unsupported number type: " + type);
    }
}

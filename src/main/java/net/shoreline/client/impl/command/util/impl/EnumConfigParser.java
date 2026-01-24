package net.shoreline.client.impl.command.util.impl;

import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.impl.command.util.IConfigParser;

@SuppressWarnings("unchecked")
public class EnumConfigParser<E extends Enum<E>> implements IConfigParser<E, EnumConfig<E>>
{
    @Override
    public boolean parseString(EnumConfig<E> config, String string)
    {
        E value = (E) Enum.valueOf(((Enum<?>) config.getValue()).getDeclaringClass(), string.toUpperCase());
        config.setValue(value);
        return true;
    }
}

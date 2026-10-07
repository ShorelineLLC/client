package net.shoreline.client.impl.command.util;

import net.shoreline.client.api.config.Config;

public interface IConfigParser<T, C extends Config<T>>
{
    boolean parseString(C config, String string);

}

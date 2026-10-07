package net.shoreline.client.impl.command.util.impl;

import net.shoreline.client.api.config.StringConfig;
import net.shoreline.client.impl.command.util.IConfigParser;

public class StringConfigParser implements IConfigParser<String, StringConfig>
{
    @Override
    public boolean parseString(StringConfig config, String string)
    {
        config.setValue(string);
        return true;
    }
}

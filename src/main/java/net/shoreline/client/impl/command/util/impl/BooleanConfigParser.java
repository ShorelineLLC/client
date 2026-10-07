package net.shoreline.client.impl.command.util.impl;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.impl.command.util.IConfigParser;

public class BooleanConfigParser implements IConfigParser<Boolean, BooleanConfig>
{
    @Override
    public boolean parseString(BooleanConfig config, String string)
    {
        if (string.equalsIgnoreCase("true"))
        {
            config.setValue(true);
            return true;
        }
        else if (string.equalsIgnoreCase("false"))
        {
            config.setValue(false);
            return true;
        }
        else if (string.equalsIgnoreCase("toggle"))
        {
            config.setValue(!config.getValue());
            return true;
        }

        return false;
    }
}

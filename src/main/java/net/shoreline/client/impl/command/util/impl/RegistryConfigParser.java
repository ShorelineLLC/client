package net.shoreline.client.impl.command.util.impl;

import net.minecraft.util.Identifier;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.impl.command.util.IConfigParser;

import java.util.Collection;

public class RegistryConfigParser<T> implements IConfigParser<Collection<T>, RegistryConfig<T>>
{
    @Override
    public boolean parseString(RegistryConfig<T> config, String string)
    {
        String action = string;
        if (action.equalsIgnoreCase("clear"))
        {
            config.clear();
            return true;
        }

        String value = "";
        if (string.contains(" "))
        {
            String[] split = string.split(" ");
            action = split[0];
            value = split[1];
        }

        if (value.isEmpty())
        {
            return false;
        }

        T element = config.getRegistry().get(Identifier.tryParse(value));
        if (element == null)
        {
            return false;
        }

        if (action.equalsIgnoreCase("add"))
        {
            config.add(element);
            return true;
        }
        else if (action.equalsIgnoreCase("remove"))
        {
            config.remove(element);
            return true;
        }

        return false;
    }
}
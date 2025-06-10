package net.shoreline.client.api.config;

import java.util.List;

public class ConfigGroup extends Config<List<Config<?>>>
{
    public ConfigGroup(String name, String description)
    {
        super(name, description);
    }


}

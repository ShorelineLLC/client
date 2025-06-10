package net.shoreline.client.api.module;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.eventbus.EventBus;

public class Toggleable extends Module
{
    final Config<Boolean> enabled = new BooleanConfig.Builder("Enabled",
            "Module enabled state").setNameAliases("Toggled", "On").setDefaultValue(false).build();

    public Toggleable(final String name,
                      final String description,
                      final GuiCategory category)
    {
        super(name, description, category);
    }

    public Toggleable(final String name,
                      final String[] nameAliases,
                      final String description,
                      final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public void enable()
    {
        EventBus.INSTANCE.subscribe(this);
        onEnable();
    }

    public void disable()
    {
        onDisable();
        EventBus.INSTANCE.unsubscribe(this);
    }

    public void toggle()
    {
        if (isEnabled())
        {
            disable();
        } else
        {
            enable();
        }
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public boolean isEnabled()
    {
        return enabled.getValue();
    }
}

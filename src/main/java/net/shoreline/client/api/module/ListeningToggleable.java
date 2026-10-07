package net.shoreline.client.api.module;

import net.shoreline.eventbus.EventBus;

public class ListeningToggleable extends Toggleable
{
    public ListeningToggleable(String name,
                               String description,
                               GuiCategory category)
    {
        super(name, description, category);
        EventBus.INSTANCE.subscribe(this);
    }

    public ListeningToggleable(final String name,
                               final String[] nameAliases,
                               final String description,
                               final GuiCategory category)
    {
        super(name, nameAliases, description, category);
        EventBus.INSTANCE.subscribe(this);
    }

    @Override
    public void enable()
    {
        enabled.setValue(true);
        EventBus.INSTANCE.dispatch(new ModuleToggleEvent(this, true));
        onEnable();
    }

    @Override
    public void disable()
    {
        onDisable();
        enabled.setValue(false);
        EventBus.INSTANCE.dispatch(new ModuleToggleEvent(this, false));
    }

    @Override
    public boolean checkNull()
    {
        return super.checkNull() || !isEnabled();
    }
}

package net.shoreline.client.impl.event.gui.click;

import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
public class ToggleGuiEvent extends Event
{
    private final ToggleModule module;

    public ToggleGuiEvent(ToggleModule module)
    {
        this.module = module;
    }

    public ToggleModule getModule()
    {
        return module;
    }

    public boolean isEnabled()
    {
        return module.isEnabled();
    }
}

package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

// HUGE Exploit
public class NoBobModule extends ToggleModule
{
    public NoBobModule()
    {
        super("NoBob", "Prevents items from bobbing when walking", ModuleCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        mc.player.horizontalSpeed = 4.0f;
    }
}

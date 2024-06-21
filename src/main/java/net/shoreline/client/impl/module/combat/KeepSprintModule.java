package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.player.SprintResetEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class KeepSprintModule extends ToggleModule
{
    public KeepSprintModule()
    {
        super("KeepSprint", "Removes sprint reset when attacking", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onSprintReset(SprintResetEvent event)
    {
        event.cancel();
    }
}

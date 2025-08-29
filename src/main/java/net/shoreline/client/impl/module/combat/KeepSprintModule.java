package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.entity.player.SprintResetEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class KeepSprintModule extends Toggleable
{
    public KeepSprintModule()
    {
        super("KeepSprint", "Keeps sprinting while attacking", GuiCategory.COMBAT);
    }

    @EventListener
    public void onSprintReset(SprintResetEvent event)
    {
        event.cancel();
    }
}

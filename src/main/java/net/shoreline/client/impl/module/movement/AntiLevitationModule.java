package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.LevitationEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class AntiLevitationModule extends ToggleModule
{

    /**
     *
     */
    public AntiLevitationModule()
    {
        super("AntiLevitation", "Prevents the player from being levitated",
                ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onLevitation(LevitationEvent event)
    {
        event.cancel();
    }
}

package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.player.LedgeClipEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class SafeWalkModule extends ToggleModule
{
    Config<Boolean> sneakConfig = register(new BooleanConfig("Sneak", "Sneaks at the edge of blocks", false));

    public SafeWalkModule()
    {
        super("SafeWalk", "Prevents you from walking off ledges", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onLedgeClip(LedgeClipEvent event)
    {
        if (!mc.player.isSneaking())
        {
            if (sneakConfig.getValue())
            {
                mc.player.setSneaking(true);
            }
            event.cancel();
            event.setClipped(true);
        }
    }
}

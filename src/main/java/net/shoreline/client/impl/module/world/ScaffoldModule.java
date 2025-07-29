package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.impl.PlacerModule;
import net.shoreline.eventbus.annotation.EventListener;

public class ScaffoldModule extends PlacerModule
{


    public ScaffoldModule()
    {
        super("Scaffold", new String[] {"BlockFly"}, "Places blocks under the player", GuiCategory.WORLD);
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {

    }
}

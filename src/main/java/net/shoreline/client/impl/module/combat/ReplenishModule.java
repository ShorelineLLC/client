package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.inventory.HotbarCache;
import net.shoreline.eventbus.annotation.EventListener;

public class ReplenishModule extends Toggleable
{
    Config<Integer> stackPercent = new NumberConfig.Builder<Integer>("Min")
            .setMin(0).setMax(99).setDefaultValue(0).setFormat("%")
            .setDescription("The minimum percent of stack before refill").build();

    private HotbarCache cache;

    public ReplenishModule()
    {
        super("Replenish", "Refills items in the hotbar", GuiCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        cache = new HotbarCache(mc.player.getInventory());


    }
}

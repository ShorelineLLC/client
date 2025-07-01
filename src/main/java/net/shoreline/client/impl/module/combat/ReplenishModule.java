package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ReplenishModule extends Toggleable
{
    Config<Integer> stackPercent = new NumberConfig.Builder<Integer>("Min")
            .setMin(0).setMax(99).setDefaultValue(0).setFormat("%")
            .setDescription("The minimum percent of stack before refill").build();

    public ReplenishModule()
    {
        super("Replenish", "Refills items in the hotbar", GuiCategory.COMBAT);
    }
}

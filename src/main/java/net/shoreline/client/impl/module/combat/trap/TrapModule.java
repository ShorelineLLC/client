package net.shoreline.client.impl.module.combat.trap;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;

public class TrapModule extends ObsidianPlacerModule
{
    protected final Config<Boolean> headConfig = new BooleanConfig.Builder("CoverHead")
            .setDescription("Traps player head")
            .setDefaultValue(false).build();

    protected final TrapPositionCalc trapPos = new TrapPositionCalc();

    public TrapModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public TrapModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }
}

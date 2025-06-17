package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class SprintModule extends Toggleable
{
    Config<SprintMode> modeConfig = new EnumConfig.Builder<SprintMode>("Mode")
            .setValues(SprintMode.values())
            .setDescription("The Sprinting mode. Rage allows for multi-directional sprinting")
            .setDefaultValue(SprintMode.LEGIT).build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates before sprinting horizontally/backwards")
            .setVisible(() -> modeConfig.getValue().equals(SprintMode.RAGE))
            .setDefaultValue(false).build();
    Config<Boolean> jumpFixConfig = new BooleanConfig.Builder("JumpFix")
            .setDescription("Fixes jumping slowdown in Rage sprint")
            .setVisible(() -> modeConfig.getValue().equals(SprintMode.RAGE))
            .setDefaultValue(false).build();

    public SprintModule()
    {
        super("Sprint", "Automatically sprints", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event)
    {

    }

    private enum SprintMode
    {
        LEGIT,
        RAGE
    }
}

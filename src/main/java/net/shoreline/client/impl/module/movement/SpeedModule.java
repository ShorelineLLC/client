package net.shoreline.client.impl.module.movement;

import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.impl.MovementModule;

public class SpeedModule extends MovementModule
{
    Config<SpeedMode> modeConfig = new EnumConfig.Builder<SpeedMode>("Mode")
            .setValues(SpeedMode.values())
            .setDescription("The mode for accelerating the player")
            .setDefaultValue(SpeedMode.VANILLA).build();

    public SpeedModule()
    {
        super("Speed", new String[] {"Strafe"}, "Move faster", GuiCategory.MOVEMENT);
    }

    @Override
    public void onMove(final Vec3d movement)
    {

    }

    public enum SpeedMode
    {
        VANILLA,
        STRAFE,
        STRAFE_STRICT
    }
}

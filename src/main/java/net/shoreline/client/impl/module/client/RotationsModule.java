package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

public class RotationsModule extends Concurrent
{
    public static RotationsModule INSTANCE;

    Config<Boolean> renderRotationsConfig = new BooleanConfig.Builder("ShowRotations")
            .setDescription("Renders the serverside rotations")
            .setDefaultValue(true).build();
    Config<MoveFix> moveFixConfig = new EnumConfig.Builder<MoveFix>("MovementFix")
            .setValues(MoveFix.values())
            .setDescription("Applies movement corrections when rotating")
            .setDefaultValue(MoveFix.OFF).build();
    Config<Boolean> gcdFixConfig = new BooleanConfig.Builder("MouseSensFix")
            .setDescription("Corrects rotations based on mouse sensitivity")
            .setDefaultValue(true).build();
    Config<Boolean> tickSyncConfig = new BooleanConfig.Builder("TickSync")
            .setDescription("Sends rotation packets every tick")
            .setDefaultValue(false).build();
    Config<Boolean> lookSyncConfig = new BooleanConfig.Builder("RotateSync")
            .setDescription("Sends rotation packets when player look changes")
            .setDefaultValue(false).build();

    public RotationsModule()
    {
        super("Rotations", "Client rotations", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    public boolean showServerRotation()
    {
        return renderRotationsConfig.getValue();
    }

    public boolean shouldApplyMoveFix()
    {
        return moveFixConfig.getValue() != MoveFix.OFF;
    }

    public boolean shouldRoundMoveFix()
    {
        return moveFixConfig.getValue() == MoveFix.NORMAL;
    }

    public enum MoveFix
    {
        NORMAL,
        GRIM,
        OFF
    }
}

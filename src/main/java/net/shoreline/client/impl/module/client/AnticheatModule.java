package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

public class AnticheatModule extends Concurrent
{
    public static AnticheatModule INSTANCE;

    Config<ACMode> acModeConfig = new EnumConfig.Builder<ACMode>("AC")
            .setValues(ACMode.values())
            .setDescription("Set this to the main anticheat of the server")
            .setDefaultValue(ACMode.VANILLA).build();
    Config<Boolean> multiTask = new BooleanConfig.Builder("Multitask")
            .setDescription("Allow using items while interacting")
            .setDefaultValue(true).build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates to face before interacting")
            .setDefaultValue(false).build();
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only places on visible faces")
            .setDefaultValue(false).build();
    Config<Integer> interactAttempts = new NumberConfig.Builder<Integer>("Limit")
            .setMin(0).setMax(100).setDefaultValue(20)
            .setDescription("Max attempts to interact on blocks").build();
    Config<Void> interactConfig = new ConfigGroup.Builder("Interact")
            .addAll(multiTask, rotateConfig, strictDirection, interactAttempts)
            .setVisible(() -> acModeConfig.getValue() != ACMode.VANILLA).build();
    Config<Boolean> raycastFixConfig = new BooleanConfig.Builder("RaytraceFix")
            .setDescription("Uses server rotations when raytracing crosshair")
            .setVisible(() -> acModeConfig.getValue() != ACMode.VANILLA)
            .setDefaultValue(false).build();
    Config<Boolean> assumeEnchanted = new BooleanConfig.Builder("AssumeBestArmor")
            .setDescription("Assumes that all enemy armor is max enchanted")
            .setVisible(() -> acModeConfig.getValue() != ACMode.VANILLA)
            .setDefaultValue(false).build();

    public AnticheatModule()
    {
        super("Anticheat", "Configure client for different anticheats", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    public boolean isAssumeEnchanted()
    {
        return assumeEnchanted.getValue();
    }

    public enum ACMode
    {
        VANILLA,
        GRIM
    }
}

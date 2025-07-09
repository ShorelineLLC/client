package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.manager.inventory.SilentSwapType;

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
    Config<Integer> bptConfig = new NumberConfig.Builder<Integer>("BlocksPerTick")
            .setMin(1).setMax(20).setDefaultValue(2)
            .setDescription("The max interactions per tick").build();
    Config<Integer> interactDelay = new NumberConfig.Builder<Integer>("Delay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between interactions").build();
    Config<Integer> interactAttempts = new NumberConfig.Builder<Integer>("Limit")
            .setMin(0).setMax(100).setDefaultValue(20)
            .setDescription("Max attempts to interact on blocks").build();
    Config<SilentSwapType> swapConfig = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for swapping to blocks")
            .setDefaultValue(SilentSwapType.HOTBAR).build();
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only places on visible faces")
            .setDefaultValue(false).build();
    Config<Void> interactConfig = new ConfigGroup.Builder("Interact")
            .addAll(multiTask, rotateConfig, swapConfig, strictDirection, interactAttempts)
            .setVisible(() -> acModeConfig.getValue() != ACMode.VANILLA).build();

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
    Config<Void> rotationConfig = new ConfigGroup.Builder("Rotations")
            .addAll(renderRotationsConfig, moveFixConfig, gcdFixConfig, tickSyncConfig, lookSyncConfig)
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

    public boolean isGrim()
    {
        return acModeConfig.getValue() == ACMode.GRIM;
    }

    public boolean isAssumeEnchanted()
    {
        return assumeEnchanted.getValue();
    }

    public int getBlocksPerTick()
    {
        return bptConfig.getValue();
    }

    public int getInteractDelay()
    {
        return interactDelay.getValue();
    }

    public int getInteractAttempts()
    {
        return interactAttempts.getValue();
    }

    public SilentSwapType getSwapType()
    {
        return swapConfig.getValue();
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

    public enum ACMode
    {
        VANILLA,
        GRIM
    }

    public enum MoveFix
    {
        NORMAL,
        GRIM,
        OFF
    }
}

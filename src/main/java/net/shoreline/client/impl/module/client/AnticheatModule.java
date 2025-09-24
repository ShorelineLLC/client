package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.ac.Anticheat;
import net.shoreline.client.impl.inventory.SilentSwapType;

@Getter
public class AnticheatModule extends Concurrent
{
    public static AnticheatModule INSTANCE;

    Config<Anticheat> acModeConfig = new EnumConfig.Builder<Anticheat>("AC")
            .setValues(Anticheat.values())
            .setDescription("Set this to the main anticheat of the server")
            .setDefaultValue(Anticheat.VANILLA).build();

    Config<Boolean> multiTask = new BooleanConfig.Builder("Multitask")
            .setDescription("Allow using items while interacting")
            .setDefaultValue(true).build();
    Config<Boolean> interactRotate = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates to face before interacting")
            .setDefaultValue(false).build();
    Config<Boolean> noGlitchBlocks = new BooleanConfig.Builder("NoGlitchBlocks")
            .setDescription("Only spawns blocks when server confirms")
            .setDefaultValue(true).build();
    Config<Boolean> attackCrystals = new BooleanConfig.Builder("Attack")
            .setDescription("Attacks crystals blocking placements")
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
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only places on visible faces")
            .setDefaultValue(false).build();
    Config<Void> interactConfig = new ConfigGroup.Builder("Interactions")
            .addAll(multiTask, interactRotate, noGlitchBlocks, attackCrystals, bptConfig,
                    interactDelay, interactAttempts, strictDirection)
            .setVisible(() -> acModeConfig.getValue() != Anticheat.VANILLA).build();

    Config<Boolean> renderRotationsConfig = new BooleanConfig.Builder("ShowRotations")
            .setDescription("Renders the serverside rotations")
            .setDefaultValue(true).build();
    Config<MoveFix> moveFixConfig = new EnumConfig.Builder<MoveFix>("MovementFix")
            .setValues(MoveFix.values())
            .setDescription("Applies movement corrections when rotating")
            .setDefaultValue(MoveFix.OFF).build();
    Config<Boolean> fixTravel = new BooleanConfig.Builder("FixInAir")
            .setDescription("Fixes the movement while in the air")
            .setVisible(() -> moveFixConfig.getValue() != MoveFix.OFF)
            .setDefaultValue(false).build();
    Config<Boolean> normalizeMovement = new BooleanConfig.Builder("Normalize")
            .setDescription("Normalizes the movement vector")
            .setVisible(() -> moveFixConfig.getValue() != MoveFix.OFF)
            .setDefaultValue(false).build();
    Config<Boolean> gcdFixConfig = new BooleanConfig.Builder("MouseSensFix")
            .setDescription("Corrects rotations based on mouse sensitivity")
            .setDefaultValue(true).build();
    Config<Boolean> tickSyncConfig = new BooleanConfig.Builder("TickSync")
            .setDescription("Sends rotation packets every tick")
            .setDefaultValue(false).build();
    Config<Boolean> lookSyncConfig = new BooleanConfig.Builder("RotateSync")
            .setDescription("Sends rotation packets when player look changes")
            .setDefaultValue(false).build();
    Config<Void> rotateConfig = new ConfigGroup.Builder("Rotations")
            .addAll(renderRotationsConfig, moveFixConfig, fixTravel, normalizeMovement, gcdFixConfig, tickSyncConfig, lookSyncConfig)
            .setVisible(() -> acModeConfig.getValue() != Anticheat.VANILLA).build();
    
    Config<SilentSwapType> silentSwap = new EnumConfig.Builder<SilentSwapType>("SilentSwap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for silent swapping to items")
            .setDefaultValue(SilentSwapType.HOTBAR)
            .build();

    Config<Boolean> raycastFixConfig = new BooleanConfig.Builder("RaytraceFix")
            .setDescription("Uses server rotations when raytracing crosshair")
            .setVisible(() -> acModeConfig.getValue() != Anticheat.VANILLA)
            .setDefaultValue(false)
            .build();

    Config<Boolean> assumeEnchanted = new BooleanConfig.Builder("AssumeBestArmor")
            .setDescription("Assumes that all enemy armor is max enchanted")
            .setVisible(() -> acModeConfig.getValue() != Anticheat.VANILLA)
            .setDefaultValue(false)
            .build();

    public AnticheatModule()
    {
        super("Anticheat", "Configure client for different anticheats", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    public SilentSwapType getSilentSwapType()
    {
        return silentSwap.getValue();
    }

    public enum MoveFix
    {
        NORMAL,
        GRIM,
        OFF
    }
}

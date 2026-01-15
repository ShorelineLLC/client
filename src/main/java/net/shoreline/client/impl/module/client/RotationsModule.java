package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
public class RotationsModule extends Concurrent 
{
    public static RotationsModule INSTANCE;
    
    Config<Boolean> renderRotationsConfig = new BooleanConfig.Builder("ShowRotations")
            .setDescription("Renders the serverside rotations")
            .setDefaultValue(true).build();
    Config<Boolean> noServerRotate = new BooleanConfig.Builder("NoRotate")
            .setDescription("Prevents the server from forcing rotations")
            .setDefaultValue(false).build();
    Config<Boolean> raycastFixConfig = new BooleanConfig.Builder("Raytrace")
            .setDescription("Uses server rotations when raytracing crosshair")
            .setDefaultValue(false).build();
    Config<MoveFix> moveFixConfig = new EnumConfig.Builder<MoveFix>("MovementFix")
            .setValues(MoveFix.values())
            .setDescription("Applies movement corrections when rotating")
            .setDefaultValue(MoveFix.OFF).build();
    Config<Boolean> fixTravel = new BooleanConfig.Builder("FixInAir")
            .setVisibilityDependant(true)
            .setDescription("Fixes the movement while in the air")
            .setVisible(() -> moveFixConfig.getValue() != MoveFix.OFF)
            .setDefaultValue(false).build();
    Config<Boolean> normalizeMovement = new BooleanConfig.Builder("Normalize")
            .setDescription("Normalizes the movement vector")
            .setVisible(() -> moveFixConfig.getValue() != MoveFix.OFF)
            .setDefaultValue(false).build();
    Config<Boolean> gcdFixConfig = new BooleanConfig.Builder("MouseSensFix")
            .setDescription("Corrects rotations based on mouse sensitivity")
            .setVisible(() -> moveFixConfig.getValue() != MoveFix.OFF)
            .setDefaultValue(true).build();
    Config<Boolean> itemFixConfig = new BooleanConfig.Builder("ItemUseFix")
            .setDescription("Fixes rotations when using items")
            .setDefaultValue(false).build();
    Config<Boolean> tickSyncConfig = new BooleanConfig.Builder("TickSync")
            .setDescription("Sends rotation packets every tick")
            .setDefaultValue(false).build();
    Config<Boolean> lookSyncConfig = new BooleanConfig.Builder("RotateSync")
            .setDescription("Sends rotation packets when player look changes")
            .setDefaultValue(false).build();

    private Rotation clientRotations;

    public RotationsModule() 
    {
        super("Rotations", "Manages client rotations", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    @EventListener
    public void onRotationUpdatePre(RotationUpdateEvent.Pre event)
    {
        if (noServerRotate.getValue() && !checkNull())
        {
            clientRotations = new Rotation(mc.player);
        }
    }

    @EventListener
    public void onRotationUpdatePre(RotationUpdateEvent.PrePacket event)
    {
        if (noServerRotate.getValue() && !checkNull())
        {
            clientRotations.applyToPlayer();
        }
    }

    public enum MoveFix
    {
        NORMAL,
        GRIM,
        OFF
    }
}

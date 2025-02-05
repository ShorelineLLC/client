package net.shoreline.client.impl.module.world;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.PlayerClimbEvent;
import net.shoreline.client.impl.event.world.BlockCollisionEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class AvoidModule extends ToggleModule
{
    Config<Boolean> voidConfig = register(new BooleanConfig("Void", "Prevents player from falling into the void", true));
    Config<Boolean> fireConfig = register(new BooleanConfig("Fire", "Prevents player from walking into fire", false));
    Config<Boolean> berryBushConfig = register(new BooleanConfig("BerryBush", "Prevents player from walking into sweet berry bushes", false));
    Config<Boolean> cactiConfig = register(new BooleanConfig("Cactus", "Prevents player from walking into cacti", false));
    Config<Boolean> unloadedConfig = register(new BooleanConfig("Unloaded", "Prevents player from entering chunks that haven't been loaded", false));
    Config<Boolean> noClimbConfig = register(new BooleanConfig("Climb", "Prevents player from climbing up blocks", false));

    public AvoidModule()
    {
        super("Avoid", "Prevents player from entering harmful areas",
                ModuleCategory.WORLD);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE && voidConfig.getValue()
                && !mc.player.isSpectator()
                && mc.player.getY() < mc.world.getBottomY())
        {
            // sendModuleMessage(Formatting.RED + "Prevented player from " +
            //    "falling into void!");
            Managers.MOVEMENT.setMotionY(0.0);
        }
    }

    @EventListener
    public void onBlockCollision(BlockCollisionEvent event)
    {
        BlockPos pos = event.getPos();
        if (fireConfig.getValue() && event.getBlock() == Blocks.FIRE
                && mc.player.getY() < pos.getY() + 1.0
                || cactiConfig.getValue()
                && event.getBlock() == Blocks.CACTUS
                || berryBushConfig.getValue()
                && event.getBlock() == Blocks.SWEET_BERRY_BUSH
                || unloadedConfig.getValue()
                && !BlockUtil.isBlockLoaded(pos.getX(), pos.getZ()))
        {
            event.cancel();
            event.setVoxelShape(VoxelShapes.fullCube());
        }
    }

    @EventListener
    public void onClimb(PlayerClimbEvent event)
    {
        if (noClimbConfig.getValue())
        {
            event.cancel();
        }
    }
}

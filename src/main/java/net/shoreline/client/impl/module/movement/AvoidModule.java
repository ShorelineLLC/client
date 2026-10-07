package net.shoreline.client.impl.module.movement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.entity.ClimbEvent;
import net.shoreline.client.impl.event.world.BlockCollisionEvent;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.world.ChunkUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AvoidModule extends Toggleable
{
    Config<Boolean> antiVoid = new BooleanConfig.Builder("Void")
            .setDescription("Prevents falling into the void")
            .setDefaultValue(false).build();
    Config<Boolean> avoidFire = new BooleanConfig.Builder("Fire")
            .setDescription("Avoids colliding with fire")
            .setDefaultValue(false).build();
    Config<Boolean> avoidCacti = new BooleanConfig.Builder("Cactus")
            .setDescription("Avoids colliding with cactus blocks")
            .setDefaultValue(false).build();
    Config<Boolean> avoidBerryBush = new BooleanConfig.Builder("BerryBush")
            .setDescription("Avoids colliding with berry bush blocks")
            .setDefaultValue(false).build();
    Config<Boolean> avoidLadders = new BooleanConfig.Builder("Ladders")
            .setDescription("Avoids climbing ladders")
            .setDefaultValue(false).build();
    Config<Boolean> avoidVines = new BooleanConfig.Builder("Vines")
            .setDescription("Avoids climbing vines")
            .setDefaultValue(false).build();
    Config<Boolean> avoidScaffolding = new BooleanConfig.Builder("Scaffolding")
            .setDescription("Avoids climbing scaffolding")
            .setDefaultValue(false).build();
    Config<Boolean> avoidUnloaded = new BooleanConfig.Builder("Unloaded")
            .setDescription("Avoids colliding with unloaded chunks")
            .setDefaultValue(false).build();

    public AvoidModule()
    {
        super("Avoid", "Prevents collisions with harmful blocks", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onBlockCollide(BlockCollisionEvent event)
    {
        if (mc.player.isSpectator())
        {
            return;
        }

        BlockState state = event.getState();
        if (state.isOf(Blocks.CACTUS) && avoidCacti.getValue()
                || state.isOf(Blocks.SWEET_BERRY_BUSH) && avoidBerryBush.getValue()
                || !ChunkUtil.isLoaded(event.getBlockPos()) && avoidUnloaded.getValue()
                || (state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE)) && mc.player.getY() == event.getBlockPos().getY() && avoidFire.getValue())
        {
            event.cancel();
            event.setCollisionShape(VoxelShapes.fullCube());
        }

        if (antiVoid.getValue())
        {
            BlockPos belowPos = EntityUtil.getRoundedBlockPos(mc.player).down();
            if (!event.getBlockPos().equals(belowPos))
            {
                return;
            }

            if (belowPos.getY() < mc.world.getBottomY())
            {
                event.cancel();
                event.setCollisionShape(VoxelShapes.fullCube());
            }
        }
    }

    @EventListener
    public void onClimb(ClimbEvent event)
    {
        if (event.getBlock() == Blocks.LADDER && avoidLadders.getValue()
                || event.getBlock() == Blocks.VINE && avoidVines.getValue()
                || event.getBlock() == Blocks.SCAFFOLDING && avoidScaffolding.getValue())
        {
            event.cancel();
        }
    }
}

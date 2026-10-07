package net.shoreline.client.impl.module.movement;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.SlowMovementEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FastWebModule extends Toggleable
{
    Config<Mode> modeConfig = new EnumConfig.Builder<Mode>("Mode")
            .setValues(Mode.values())
            .setDescription("The mode for speeding up movement")
            .setDefaultValue(Mode.NORMAL).build();
    Config<Float> grimRange = new NumberConfig.Builder<Float>("Range")
            .setMin(0.0f).setDefaultValue(1.0f).setMax(3.0f).setFormat("m")
            .setVisible(() -> modeConfig.getValue() == Mode.GRIM)
            .setDescription("The grim web range").build();
    Config<Float> horizontalFactor = new NumberConfig.Builder<Float>("H-Factor")
            .setMin(0.0f).setDefaultValue(1.0f).setMax(1.0f)
            .setDescription("The horizontal move speed").build();
    Config<Float> verticalFactor = new NumberConfig.Builder<Float>("V-Factor")
            .setMin(0.0f).setDefaultValue(1.0f).setMax(1.0f)
            .setDescription("The vertical move speed").build();

    public FastWebModule()
    {
        super("FastWeb", "Move through webs faster", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTickPre(TickEvent.Pre event)
    {
        if (checkNull() || modeConfig.getValue() != Mode.GRIM)
        {
            return;
        }

        Box bb = mc.player.getBoundingBox().expand(grimRange.getValue());
        Set<BlockPos> webBlocks = BlockPos.stream(bb)
                .map(BlockPos::toImmutable)
                .filter(p -> mc.world.getBlockState(p).getBlock() == Blocks.COBWEB)
                .collect(Collectors.toCollection(HashSet::new));

        for (BlockPos pos : webBlocks)
        {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
        }
    }

    @EventListener
    public void onSlowMovement(SlowMovementEvent event)
    {
        if (event.getBlockState().getBlock() != Blocks.COBWEB)
        {
            return;
        }

        event.cancel();
        event.setMultiplier(horizontalFactor.getValue() == 1.0f && verticalFactor.getValue() == 1.0f ? Vec3d.ZERO :
                new Vec3d(horizontalFactor.getValue(),
                        verticalFactor.getValue(),
                        horizontalFactor.getValue()));
    }

    private enum Mode
    {
        NORMAL,
        GRIM
    }
}

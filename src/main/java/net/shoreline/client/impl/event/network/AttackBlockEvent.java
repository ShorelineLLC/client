package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Cancelable
@Getter
public class AttackBlockEvent extends Event
{
    private final BlockPos pos;
    private final Direction direction;

    public BlockState getState()
    {
        return MinecraftClient.getInstance().world.getBlockState(pos);
    }
}

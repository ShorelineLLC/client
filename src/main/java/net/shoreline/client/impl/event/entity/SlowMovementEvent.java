package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
@RequiredArgsConstructor
public class SlowMovementEvent extends Event
{
    private final BlockState blockState;
    private Vec3d multiplier;

    @Cancelable
    @RequiredArgsConstructor
    @Getter
    public static class Block extends Event
    {
        private final net.minecraft.block.Block block;
    }
}

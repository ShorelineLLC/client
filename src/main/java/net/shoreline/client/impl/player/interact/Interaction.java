package net.shoreline.client.impl.player.interact;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Getter
@Builder
public class Interaction
{
    private final BlockPos pos;
    private final Direction direction;
    private final Hand hand;
    private final boolean packetPlace;
}

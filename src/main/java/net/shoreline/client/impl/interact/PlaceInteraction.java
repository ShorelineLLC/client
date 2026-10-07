package net.shoreline.client.impl.interact;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.world.AirPlaceModule;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlaceInteraction extends Interaction<BlockPos>
{
    public static final AtomicInteger GLOBAL_COUNT = new AtomicInteger();

    private final Block block;
    private final boolean airPlace;

    private Direction direction;

    public PlaceInteraction(BlockPos pos,
                            Block block,
                            Direction direction,
                            Hand hand,
                            boolean clientInteract,
                            boolean airPlace)
    {
        super("BlockPlaceInteraction", pos, hand, clientInteract);
        this.block = block;
        this.direction = direction;
        this.airPlace = airPlace;
    }

    public PlaceInteraction(BlockPos pos,
                            Block block,
                            Direction direction,
                            Hand hand)
    {
        this(pos, block, direction, hand, false, AirPlaceModule.INSTANCE.isForceAirPlace());
    }

    public static PlaceInteraction.Builder builder()
    {
        return new PlaceInteraction.Builder();
    }

    public BlockPos getPos()
    {
        return getInteract();
    }

    public BlockPos getInteractPos()
    {
        return airPlace ? interact : interact.offset(direction.getOpposite());
    }

    public Vec3d getInteractVec()
    {
        return getInteractPos().toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
    }

    public BlockState getState()
    {
        return MinecraftClient.getInstance().world.getBlockState(getPos());
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof PlaceInteraction i && i.getInteract().equals(getInteract());
    }

    @Override
    public ActionResult applyInteraction()
    {
        Box box = new Box(getPos());
        BlockHitResult result = new BlockHitResult(
                getInteractVec(),
                direction,
                getInteractPos(),
                box.contains(mc.player.getEyePos()));

        if (!clientInteract || !mc.isOnThread())
        {
            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
            Managers.INTERACT.playBlockPlaceSound(interact, getState());
            return ActionResult.SUCCESS;
        } else
        {
            return mc.interactionManager.interactBlock(mc.player, hand, result);
        }
    }

    public static class Builder
    {
        private BlockPos pos;
        private Block block;
        private Direction direction;
        private Hand hand;
        private boolean clientInteract;
        private boolean airPlace;

        public Builder pos(BlockPos pos)
        {
            this.pos = pos;
            return this;
        }

        public Builder block(Block block)
        {
            this.block = block;
            return this;
        }

        public Builder direction(Direction direction)
        {
            this.direction = direction;
            return this;
        }

        public Builder hand(Hand hand)
        {
            this.hand = hand;
            return this;
        }

        public Builder clientInteract(boolean clientInteract)
        {
            this.clientInteract = clientInteract;
            return this;
        }

        public Builder airPlace(boolean airPlace)
        {
            this.airPlace = airPlace;
            return this;
        }

        public PlaceInteraction build()
        {
            return new PlaceInteraction(
                    pos,
                    block,
                    direction,
                    hand,
                    clientInteract,
                    airPlace
            );
        }
    }
}

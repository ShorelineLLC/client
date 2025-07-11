package net.shoreline.client.impl.interact;

import net.minecraft.block.Block;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InteractManager extends GenericFeature
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;
    private final AirPlaceModule airPlace = AirPlaceModule.INSTANCE;

    private final ConcurrentMap<BlockPos, Long> placedBlocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<Entity, Integer> placedEntityIds = new ConcurrentHashMap<>();

    public InteractManager()
    {
        super("Interactions");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && !packet.getState().isAir())
        {
            placedBlocks.remove(packet.getPos());
        }
    }

    public void placeBlock(Interaction interaction)
    {
        final BlockPos blockPos = interaction.getPos();
        List<Entity> entitiesBlocking = getEntitiesBlocking(blockPos, interaction.getBlock());
        if (!entitiesBlocking.isEmpty())
        {
            return;
        }

        placeBlockInternal(interaction);

        placedBlocks.put(blockPos, System.currentTimeMillis());
        entitiesBlocking.forEach(e -> placedEntityIds.merge(e, 1, Integer::sum));
    }

    public boolean canPlaceBlock(BlockPos blockPos, Block block)
    {
        return getEntitiesBlocking(blockPos, block).isEmpty();
    }

    private boolean checkBlockDelay(BlockPos blockPos)
    {
        int bps = anticheat.getBlocksPerTick() * 20;
        long currTime = System.currentTimeMillis();
        if (placedBlocks.values().stream().filter(x -> currTime - x <= 1000L).count() >= bps)
        {
            return true;
        }

        return currTime - placedBlocks.getOrDefault(blockPos, 0L) < anticheat.getInteractDelay();
    }

    public List<Entity> getEntitiesBlocking(BlockPos blockPos, Block block)
    {
        final List<Entity> entities = new ArrayList<>();
        final VoxelShape shape = block.getDefaultState()
                .getCollisionShape(mc.world, blockPos, ShapeContext.absent())
                .offset(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if (shape.isEmpty())
        {
            return entities;
        }

        for (Entity entity : mc.world.getOtherEntities(null, shape.getBoundingBox()))
        {
            if (entity.isRemoved() || !entity.intersectionChecked)
            {
                continue;
            } else if (!VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND))
            {
                continue;
            }

            if (entity instanceof EndCrystalEntity && placedEntityIds.getOrDefault(entity, 0) > anticheat.getInteractAttempts())
            {
                entities.add(entity);
            }
        }

        return entities;
    }

    private void placeBlockInternal(Interaction interaction)
    {
        Direction direction = interaction.getDirection();
        Hand hand = interaction.getHand();

        boolean airPlacing = false;

        boolean noValidDir = airPlace.isForceAirPlace() || direction == null;
        if (hand == Hand.MAIN_HAND && noValidDir && airPlace.isEnabled())
        {
            airPlacing = true;
            direction = Direction.DOWN;
            if (airPlace.isGrim())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
                hand = Hand.OFF_HAND;
            }
        }

        if (direction == null)
        {
            return;
        }

        BlockPos blockPos = interaction.getPos().offset(direction.getOpposite());

        PlayerInput playerInput = mc.player.input.playerInput;
        boolean shouldSneak = !airPlacing && BlockUtil.isInteractable(blockPos) && !playerInput.sneak();
        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(playerInput, true);
        }

        ActionResult actionResult;
        Vec3d interactionVec = blockPos.toCenterPos().add(interaction.getHitVec());
        if (anticheat.shouldInteractRotate())
        {
            float[] rots = RotationUtil.getRotationsTo(mc.player.getEyePos(), interactionVec);
            Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
        }

        BlockHitResult result = new BlockHitResult(interactionVec, direction, blockPos, false);
        if (interaction.isPacketPlace())
        {
            Hand finalHand = hand;
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(finalHand, result, id));
            actionResult = ActionResult.SUCCESS;
        } else
        {
            actionResult = mc.interactionManager.interactBlock(mc.player, hand, result);
        }

        if (actionResult instanceof ActionResult.Success r && r.swingSource() == ActionResult.SwingSource.CLIENT)
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(interaction.getHand()));
        }

        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(playerInput, false);
        }

        if (airPlacing && airPlace.isGrim())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
        }
    }

    public void endPlacement()
    {
        if (anticheat.shouldInteractRotate())
        {
            Managers.ROTATION.setSilentRotation(new Rotation(mc.player));
        }
    }
}

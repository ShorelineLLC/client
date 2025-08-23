package net.shoreline.client.impl.interact;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.world.BlockUtil;

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

    private boolean placementLock;

    public InteractManager()
    {
        super("Interactions");
    }

    public boolean placeBlock(Interaction interaction)
    {
        final BlockPos blockPos = interaction.getPos();
        if (!mc.world.isInBuildLimit(blockPos))
        {
            return false;
        }

        placedBlocks.values().removeIf(t -> System.currentTimeMillis() - t > 1000);
        if (placedBlocks.size() >= anticheat.getBptConfig().getValue() * 20)
        {
            return false;
        }

        if (System.currentTimeMillis() - placedBlocks.getOrDefault(blockPos, 0L) < anticheat.getInteractDelay().getValue())
        {
            return false;
        }

        List<EndCrystalEntity> crystalsBlocking = getCrystalsBlocking(blockPos, interaction.getBlock());
        if (!crystalsBlocking.isEmpty())
        {
            return false;
        }

        boolean result = placeBlockInternal(interaction);

        placedBlocks.put(blockPos, System.currentTimeMillis());
        crystalsBlocking.forEach(e -> placedEntityIds.merge(e, 1, Integer::sum));
        return result;
    }

    public boolean canPlaceBlock(BlockPos blockPos, Block block)
    {
        return getCrystalsBlocking(blockPos, block).isEmpty();
    }

    public List<EndCrystalEntity> getCrystalsBlocking(BlockPos blockPos, Block block)
    {
        final BlockState state = block.getDefaultState();
        final List<EndCrystalEntity> crystalEntities = new ArrayList<>();
        final VoxelShape shape = state.getCollisionShape(mc.world, blockPos, ShapeContext.absent()).offset(new Vec3d(blockPos));

        if (shape.isEmpty())
        {
            return crystalEntities;
        }

        for (Entity entity : mc.world.getOtherEntities(null, shape.getBoundingBox()))
        {
            if (!entity.isRemoved() && entity.intersectionChecked
                    && VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND))
            {
                continue;
            }

            if (entity instanceof EndCrystalEntity crystalEntity && placedEntityIds.getOrDefault(entity, 0) > anticheat.getInteractAttempts().getValue())
            {
                crystalEntities.add(crystalEntity);
            }
        }

        return crystalEntities;
    }

    private boolean placeBlockInternal(Interaction interaction)
    {
        Direction direction = interaction.getDirection();
        Hand hand = interaction.getHand();

        boolean airPlacing = false;

        boolean noValidDir = airPlace.isForceAirPlace() || direction == null;
        if (hand == Hand.MAIN_HAND && noValidDir && airPlace.isEnabled())
        {
            airPlacing = true;
            direction = Direction.DOWN;
            interaction.setDirection(direction);

            if (airPlace.isGrim())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
                hand = Hand.OFF_HAND;
            }
        }

        if (direction == null)
        {
            return false;
        }

        Vec3d eyePos = mc.player.getEyePos();
        Box box = new Box(interaction.getPos());
        BlockPos blockPos = interaction.getPos().offset(direction.getOpposite());

        PlayerInput playerInput = mc.player.input.playerInput;
        boolean shouldSneak = !airPlacing && BlockUtil.isInteractable(blockPos) && !playerInput.sneak();
        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(playerInput, true);
        }

        ActionResult actionResult;
        Vec3d interactionVec = blockPos.toCenterPos().add(interaction.getHitVec());
        if (anticheat.getInteractRotate().getValue())
        {
            float[] rots = RotationUtil.getRotationsTo(eyePos, interactionVec);
            Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
        }

        BlockHitResult result = new BlockHitResult(interactionVec, direction, blockPos, box.contains(eyePos));
        if (interaction.isPacketPlace())
        {
            Hand finalHand = hand;
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(finalHand, result, id));
            actionResult = ActionResult.SUCCESS;
        } else
        {
            actionResult = mc.interactionManager.interactBlock(mc.player, hand, result);
        }

        if (actionResult instanceof ActionResult.Success)
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

        return true;
    }

    public boolean startPlacement(int slot)
    {
        if (placementLock || slot == -1)
        {
            return false;
        }

        if (mc.player.isUsingItem() && !anticheat.getMultiTask().getValue())
        {
            return false;
        }

        if (!Managers.INVENTORY.startSwap(slot, SilentSwapType.HOTBAR))
        {
            return false;
        }

        return placementLock = true;
    }

    public void endPlacement()
    {
        if (!placementLock)
        {
            return;
        }

        if (anticheat.getInteractRotate().getValue())
        {
            Managers.ROTATION.resetSilentRotation();
        }

        Managers.INVENTORY.endSwap(SilentSwapType.HOTBAR);
        placementLock = false;
    }
}

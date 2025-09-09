package net.shoreline.client.impl.interact;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.combat.AuraModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.world.BlockUtil;

import java.util.ArrayList;
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

        if (isEntityBlocking(blockPos, interaction.getBlock(), true))
        {
            return false;
        }

        boolean result = placeBlockInternal(interaction);
        if (result)
        {
            placedBlocks.put(blockPos, System.currentTimeMillis());
        }

        return result;
    }

    public boolean canPlaceBlock(BlockPos blockPos, Block block)
    {
        return !isEntityBlocking(blockPos, block, false);
    }

    public boolean isEntityBlocking(BlockPos blockPos, Block block, boolean merge)
    {
        final BlockState state = block.getDefaultState();
        final VoxelShape shape = state.getCollisionShape(mc.world, blockPos, ShapeContext.absent()).offset(Vec3d.of(blockPos));
        if (shape.isEmpty())
        {
            return false;
        }

        boolean attacked = false;
        for (Entity entity : new ArrayList<>(mc.world.getOtherEntities(null, shape.getBoundingBox())))
        {
            if (entity.isRemoved() || !entity.intersectionChecked)
            {
                continue;
            }

            if (!VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND))
            {
                continue;
            }

            if (entity instanceof EndCrystalEntity && placedEntityIds.getOrDefault(entity, 0) <= anticheat.getInteractAttempts().getValue())
            {
                if (merge)
                {
                    if (anticheat.getAttackCrystals().getValue() && !attacked)
                    {
                        AuraModule.INSTANCE.sendAttackPackets(entity, false);
                        attacked = true;
                    }

                    placedEntityIds.merge(entity, 1, Integer::sum);
                }

                continue;
            }

            return true;
        }

        return false;
    }

    private boolean placeBlockInternal(Interaction interaction)
    {
        BlockPos placePos = interaction.getPos();
        Direction direction = interaction.getDirection();

        boolean noValidDir = airPlace.isForceAirPlace() || direction == null;
        boolean airPlacing = interaction.getHand() == Hand.MAIN_HAND && noValidDir && airPlace.isEnabled();
        if (airPlacing)
        {
            direction = Direction.DOWN;
            interaction.setDirection(direction);

            if (airPlace.isGrim())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
            }
        }

        if (direction == null)
        {
            return false;
        }

        Vec3d eyePos = mc.player.getEyePos();
        Box box = new Box(placePos);
        BlockPos blockPos = airPlacing ? placePos : placePos.offset(direction.getOpposite());

        boolean shouldSneak = !airPlacing && BlockUtil.isInteractable(blockPos) && !mc.player.isSneaking();
        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(true);
        }

        ActionResult actionResult;
        Vec3d interactionVec = blockPos.toCenterPos().add(interaction.getHitVec());
        if (anticheat.getInteractRotate().getValue())
        {
            float[] rots = RotationUtil.getRotationsTo(eyePos, interactionVec);
            Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
        }

        Hand hand = airPlacing && airPlace.isGrim() ? Hand.OFF_HAND : interaction.getHand();
        BlockHitResult result = new BlockHitResult(interactionVec, direction, blockPos, box.contains(eyePos));
        if (interaction.isPacketPlace())
        {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
            actionResult = ActionResult.SUCCESS;
        } else
        {
            actionResult = mc.interactionManager.interactBlock(mc.player, hand, result);
        }

        if (actionResult.isAccepted())
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }

        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(false);
        }

        if (airPlacing && airPlace.isGrim())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
        }

        return actionResult.isAccepted();
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
        if (anticheat.getInteractRotate().getValue())
        {
            Managers.ROTATION.resetSilentRotation();
        }

        Managers.INVENTORY.endSwap(SilentSwapType.HOTBAR);
        placementLock = false;
    }

    public void interactItem(Hand hand, boolean swing)
    {
        Rotation playerRotation = Managers.ROTATION.hasClientRotation() ? Managers.ROTATION.getClientRotation() : new Rotation(mc.player);
        interactItem(hand, playerRotation.getYaw(), playerRotation.getPitch(), swing);
    }

    public void interactItem(Hand hand, float yaw, float pitch, boolean swing)
    {
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(hand, id, yaw, pitch));
        if (swing)
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }
    }
}

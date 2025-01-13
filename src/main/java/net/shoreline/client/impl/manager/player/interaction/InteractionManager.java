package net.shoreline.client.impl.manager.player.interaction;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.world.SneakBlocks;
import net.shoreline.eventbus.EventBus;

/**
 * @author xgraza
 * @since 1.0
 */
public final class InteractionManager implements Globals
{
    public InteractionManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    public boolean placeBlock(final BlockPos pos,
                              final int slot,
                              final boolean grim,
                              final boolean strictDirection,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback)
    {
        return placeBlock(pos, slot, grim, strictDirection, clientSwing, rotationCallback, false);
    }

    public boolean placeBlock(final BlockPos pos,
                              final int slot,
                              final boolean grim,
                              final boolean strictDirection,
                              final boolean clientSwing,
                              final RotationCallback rotationCallback,
                              final boolean airPlace)
    {
        Direction direction = getInteractDirectionInternal(pos, strictDirection);
        if (airPlace || AirPlaceModule.getInstance().isEnabled() && direction == null)
        {
            direction = Direction.DOWN;
            return placeBlock(pos, direction, slot, clientSwing, grim, rotationCallback);
        }
        if (direction == null)
        {
            return false;
        }
        final BlockPos neighbor = pos.offset(direction.getOpposite());
        return placeBlock(neighbor, direction, slot, clientSwing, false, rotationCallback);
    }

    public boolean placeBlock(final BlockPos pos,
                              final int slot,
                              final boolean grim,
                              final boolean strictDirection,
                              final boolean clientSwing,
                              final boolean packet,
                              final RotationCallback rotationCallback)
    {
        return placeBlock(pos, slot, grim, strictDirection, clientSwing, packet, false, rotationCallback);
    }

    public boolean placeBlock(final BlockPos pos,
                              final int slot,
                              final boolean grim,
                              final boolean strictDirection,
                              final boolean clientSwing,
                              final boolean packet,
                              final boolean airPlace,
                              final RotationCallback rotationCallback)
    {
        Direction direction = getInteractDirectionInternal(pos, strictDirection);
        if (airPlace || AirPlaceModule.getInstance().isEnabled() && direction == null)
        {
            direction = Direction.DOWN;
            return placeBlock(pos, direction, slot, clientSwing, grim, rotationCallback);
        }
        if (direction == null)
        {
            return false;
        }
        final BlockPos neighbor = pos.offset(direction.getOpposite());
        return placeBlock(neighbor, direction, slot, clientSwing, false, packet, rotationCallback);
    }

    public boolean placeBlock(final BlockPos pos,
                              final Direction direction,
                              final int slot,
                              final boolean clientSwing,
                              final boolean grimAirPlace,
                              final boolean packet,
                              final RotationCallback rotationCallback)
    {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        return placeBlock(new BlockHitResult(hitVec, direction, pos, false),
                slot, clientSwing, grimAirPlace, packet, rotationCallback);
    }

    public boolean placeBlock(final BlockPos pos,
                              final Direction direction,
                              final int slot,
                              final boolean clientSwing,
                              final boolean grimAirPlace,
                              final RotationCallback rotationCallback)
    {
        Vec3d hitVec = pos.toCenterPos().add(new Vec3d(direction.getUnitVector()).multiply(0.5));
        return placeBlock(new BlockHitResult(hitVec, direction, pos, false),
                slot, clientSwing, grimAirPlace, rotationCallback);
    }

    public boolean placeBlock(final BlockHitResult hitResult,
                              final int slot,
                              final boolean clientSwing,
                              final boolean grimAirPlace,
                              final boolean packet,
                              final RotationCallback rotationCallback)
    {
        final boolean isSpoofing = slot != Managers.INVENTORY.getServerSlot();
        if (isSpoofing)
        {
            Managers.INVENTORY.setSlot(slot);
            // mc.player.getInventory().selectedSlot = slot;
        }

        if (grimAirPlace)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        }

        final boolean isRotating = rotationCallback != null;
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(true, angles);
        }

        final boolean result = placeBlockImmediately(hitResult, grimAirPlace ? Hand.OFF_HAND : Hand.MAIN_HAND, clientSwing, packet);
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(false, angles);
        }

        if (grimAirPlace)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        }

        if (isSpoofing)
        {
            Managers.INVENTORY.syncToClient();
            //mc.player.getInventory().selectedSlot = previousSlot;
        }

        return result;
    }

    public boolean placeBlock(final BlockHitResult hitResult,
                              final int slot,
                              final boolean clientSwing,
                              final boolean grimAirPlace,
                              final RotationCallback rotationCallback)
    {
        final boolean isSpoofing = slot != Managers.INVENTORY.getServerSlot();
        if (isSpoofing)
        {
            Managers.INVENTORY.setSlot(slot);
            // mc.player.getInventory().selectedSlot = slot;
        }

        if (grimAirPlace)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        }

        final boolean isRotating = rotationCallback != null;
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(true, angles);
        }

        final boolean result = placeBlockImmediately(hitResult, grimAirPlace ? Hand.OFF_HAND : Hand.MAIN_HAND, clientSwing, true);
        if (isRotating)
        {
            float[] angles = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitResult.getPos());
            rotationCallback.handleRotation(false, angles);
        }

        if (grimAirPlace)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        }

        if (isSpoofing)
        {
            Managers.INVENTORY.syncToClient();
            //mc.player.getInventory().selectedSlot = previousSlot;
        }

        return result;
    }

    public boolean placeBlockImmediately(final BlockHitResult result,
                                         final Hand hand,
                                         final boolean clientSwing,
                                         final boolean packet)
    {
        final BlockState state = mc.world.getBlockState(result.getBlockPos());
        final boolean shouldSneak = SneakBlocks.isSneakBlock(state) && !mc.player.isSneaking();
        if (shouldSneak)
        {
            Managers.MOVEMENT.setPacketSneaking(true);
            MovementUtil.applySneak();
        }
        final ActionResult actionResult = packet ? placeBlockPacket(result, hand) : placeBlockInternally(result, hand);
        if (actionResult.isAccepted() && actionResult.shouldSwingHand())
        {
            if (clientSwing)
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            else
            {
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
        if (shouldSneak)
        {
            Managers.MOVEMENT.setPacketSneaking(false);
        }
        return actionResult.isAccepted();
    }

    private ActionResult placeBlockInternally(final BlockHitResult hitResult,
                                              final Hand hand)
    {
        return mc.interactionManager.interactBlock(mc.player, hand, hitResult);
        // Managers.NETWORK.sendSequencedPacket(sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, sequence));
        // return ((AccessorClientPlayerInteractionManager) mc.interactionManager).hookInteractBlockInternal(mc.player, Hand.MAIN_HAND, hitResult);
    }

    public ActionResult placeBlockPacket(final BlockHitResult hitResult,
                                         final Hand hand)
    {
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, hitResult, id));
        return ActionResult.SUCCESS;
    }

    /**
     * @param blockPos
     * @param strictDirection
     * @return
     */
    public Direction getInteractDirection(final BlockPos blockPos, final boolean strictDirection)
    {
        Direction dir = getInteractDirectionInternal(blockPos, strictDirection);
        if (dir != null)
        {
            return dir;
        }
        return Direction.UP;
    }

    public Direction getInteractDirectionInternal(final BlockPos blockPos, final boolean strictDirection)
    {
        Direction interactDirection = null;
        for (final Direction direction : Direction.values())
        {
            final BlockState state = mc.world.getBlockState(blockPos.offset(direction));
            if (state.isAir() || !state.getFluidState().isEmpty())
            {
                continue;
            }
            if (strictDirection && !canSeeFace(blockPos, direction.getOpposite()))
            {
                continue;
            }
            interactDirection = direction;
            break;
        }
        if (interactDirection == null)
        {
            return null;
        }
        return interactDirection.getOpposite();
    }

    public boolean canSeeFace(final BlockPos target, final Direction face)
    {
        final Vec3d eyes = mc.player.getEyePos();
        final Vec3i dir = face.getVector();
        final Vec3d scaled = new Vec3d(dir.getX() * 0.5, dir.getY() * 0.5, dir.getZ() * 0.5);
        final Vec3d pos = target.toCenterPos().add(scaled);
        switch (face)
        {
            case NORTH:
            {
                return eyes.z < pos.z;
            }
            case EAST:
            {
                return eyes.x > pos.x;
            }
            case SOUTH:
            {
                return eyes.z > pos.z;
            }
            case WEST:
            {
                return eyes.x < pos.x;
            }
            case UP:
            {
                return eyes.y + 0.5 > pos.y;
            }
            case DOWN:
            {
                return eyes.y < pos.y;
            }
            default:
            {
                return false;
            }
        }
    }

    /**
     * Checks if the block is within our "eye range"
     * You can't place blocks above your head for any direction other than DOWN
     *
     * @param pos the block position
     * @return if the block pos is in range of our eye y coordinate
     */
    public boolean isInEyeRange(final BlockPos pos)
    {
        return pos.getY() > mc.player.getY() + mc.player.getStandingEyeHeight();
    }
}

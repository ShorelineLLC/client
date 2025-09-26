package net.shoreline.client.impl.interact;

import com.google.common.collect.Lists;
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
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.imixin.IClientPlayerInteractionManager;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.KillAuraModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.impl.module.world.InteractionsModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.world.BlockUtil;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InteractManager extends NetworkHandler
{
    private final InteractionsModule interactConfig = InteractionsModule.INSTANCE;
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

        if (SpeedMineModule.INSTANCE.isEnabled())
        {
            MiningData mining = SpeedMineModule.INSTANCE.getMainMiningBlock();
            if (mining != null && mining.isDoneMining() && mining.getBlockPos().equals(blockPos))
            {
                return false;
            }
        }

        placedBlocks.values().removeIf(t -> System.currentTimeMillis() - t > 1000);
        if (placedBlocks.size() >= interactConfig.getBptConfig().getValue() * 20)
        {
            return false;
        }

        if (System.currentTimeMillis() - placedBlocks.getOrDefault(blockPos, 0L) < interactConfig.getInteractDelay().getValue())
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

        for (Entity entity : collectEntitiesInBox(shape.getBoundingBox()))
        {
            if (entity.isRemoved() || !entity.intersectionChecked)
            {
                continue;
            }

            if (!VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND))
            {
                continue;
            }

            if (entity instanceof EndCrystalEntity && placedEntityIds.getOrDefault(entity, 0) <= interactConfig.getInteractAttempts().getValue())
            {
                if (merge)
                {
                    if (interactConfig.getAttackCrystals().getValue() && !attacked)
                    {
                        KillAuraModule.INSTANCE.sendAttackPackets(entity, false);
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
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
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

        MutableObject<ActionResult> actionResult = new MutableObject<>();

        Vec3d interactionVec = blockPos.toCenterPos().add(interaction.getHitVec());
        if (interactConfig.getInteractRotate().getValue())
        {
            float[] rots = RotationUtil.getRotationsTo(eyePos, interactionVec);
            Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
        }

        Hand hand = airPlacing && airPlace.isGrim() ? Hand.OFF_HAND : interaction.getHand();
        BlockHitResult result = new BlockHitResult(interactionVec, direction, blockPos, box.contains(eyePos));

        if (interaction.isPacketPlace() || !mc.isOnThread())
        {
            sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
            actionResult.setValue(ActionResult.SUCCESS);
        } else
        {
            actionResult.setValue(mc.interactionManager.interactBlock(mc.player, hand, result));
        }

        boolean success = actionResult.getValue() != null && actionResult.getValue().isAccepted();
        if (success)
        {
            sendPacket(new HandSwingC2SPacket(hand));
        }

        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(false);
        }

        if (airPlacing && airPlace.isGrim())
        {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, direction));
        }

        return success;
    }

    public boolean startPlacement(int slot)
    {
        if (placementLock || slot == -1)
        {
            return false;
        }

        if (mc.player.isUsingItem() && !interactConfig.getMultiTask().getValue())
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
        if (interactConfig.getInteractRotate().getValue())
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
        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(hand, id, yaw, pitch));
        if (swing)
        {
            sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    private List<Entity> collectEntitiesInBox(Box boundingBox)
    {
        List<Entity> entities = Lists.newArrayList();
        for (Entity entity : mc.world.getEntities())
        {
            if (entity.getBoundingBox().intersects(boundingBox))
            {
                entities.add(entity);
            }
        }

        return entities;
    }
}

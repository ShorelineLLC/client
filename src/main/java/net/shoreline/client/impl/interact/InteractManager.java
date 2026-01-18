package net.shoreline.client.impl.interact;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.world.BlockCollisionEvent;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.client.InteractionsModule;
import net.shoreline.client.impl.module.combat.KillAuraModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.client.util.world.WorldUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractManager extends NetworkHandler
{
    private final InteractionsModule interactConfig = InteractionsModule.INSTANCE;
    private final AirPlaceModule airPlace = AirPlaceModule.INSTANCE;

    private final ConcurrentMap<PlaceInteraction, Long> placeInteractions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Entity, Integer> placedEntityIds = new ConcurrentHashMap<>();

    private final AtomicInteger blocksPlaced = new AtomicInteger();

    private boolean placementLock;

    public InteractManager()
    {
        super("Interactions");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onTickPost(TickEvent.Post event)
    {
        blocksPlaced.set(0);
        placeInteractions.values().removeIf(t -> System.currentTimeMillis() - t > 1000);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            for (PlaceInteraction placeInteraction : placeInteractions.keySet())
            {
                if (!placeInteraction.getInteract().equals(packet.getPos()))
                {
                    continue;
                }

                // Confirm that we succeeded placement serverside
                placeInteraction.setStatus(packet.getState().isOf(placeInteraction.getBlock()) ?
                        InteractStatus.SERVER_CONFIRMED : InteractStatus.SERVER_MISMATCH);
                break;
            }
        }
    }

    @EventListener
    public void onBlockCollide(BlockCollisionEvent event)
    {
        if (checkNull() || !event.getState().isAir() || !interactConfig.getSimulation().getValue())
        {
            return;
        }

        for (PlaceInteraction placeInteraction : placeInteractions.keySet())
        {
            if (placeInteraction.getStatus() != InteractStatus.UNCONFIRMED || !placeInteraction.getInteract().equals(event.getBlockPos()))
            {
                continue;
            }

            VoxelShape collisionShape = placeInteraction.getBlock().getDefaultState().getCollisionShape(mc.world, event.getBlockPos());
            event.cancel();
            event.setCollisionShape(collisionShape);
            return;
        }
    }

    public boolean placeBlock(PlaceInteraction placeInteraction)
    {
        final BlockPos blockPos = placeInteraction.getPos();
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

        placeInteractions.values().removeIf(t -> System.currentTimeMillis() - t > 1000);
        if (check(blockPos) || isEntityBlocking(blockPos, placeInteraction.getBlock(), true))
        {
            return false;
        }

        boolean result = placeBlockInternal(placeInteraction);
        if (result)
        {
            placeInteractions.put(placeInteraction, System.currentTimeMillis());
            blocksPlaced.incrementAndGet();
        }

        return result;
    }

    public boolean check(BlockPos blockPos)
    {
        if (blocksPlaced.get() > interactConfig.getBptConfig().getValue())
        {
            return true;
        }

        Optional<Map.Entry<PlaceInteraction, Long>> interact = placeInteractions.entrySet().stream()
                .filter(d -> d.getKey().getInteract().equals(blockPos))
                .findFirst();

        return interact.isPresent() && System.currentTimeMillis() - interact.get().getValue() < interactConfig.getInteractDelay().getValue();
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

        for (Entity entity : WorldUtil.collectEntitiesInBox(shape.getBoundingBox()))
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

    private boolean placeBlockInternal(PlaceInteraction placeInteraction)
    {
        Direction direction = placeInteraction.getDirection();
        boolean airPlacing = placeInteraction.getHand() == Hand.MAIN_HAND && direction == null && airPlace.isEnabled() && !airPlace.isForceAirPlace();
        if (airPlacing)
        {
            direction = Direction.DOWN;
            placeInteraction.setDirection(direction);

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
        boolean shouldSneak = !airPlacing && BlockUtil.isInteractable(placeInteraction.getInteractPos()) && !mc.player.isSneaking();
        if (shouldSneak)
        {
            Managers.MOVEMENT.setSilentSneaking(true);
        }

        if (interactConfig.getInteractRotate().getValue())
        {
            float[] rots = RotationUtil.getRotationsTo(eyePos, placeInteraction.getInteractVec());
            Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
        }

        if (airPlacing && airPlace.isGrim())
        {
            placeInteraction.setHand(Hand.OFF_HAND);
        }

        ActionResult actionResult = placeInteraction.applyInteraction();

        boolean success = actionResult != null && actionResult.isAccepted();
        if (success)
        {
            sendPacket(new HandSwingC2SPacket(placeInteraction.getHand()));
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

    public void interactItem(ItemInteraction itemInteraction)
    {
        ActionResult actionResult = itemInteraction.applyInteraction();
        boolean success = actionResult != null && actionResult.isAccepted();
        if (success)
        {
            sendPacket(new HandSwingC2SPacket(itemInteraction.getHand()));
        }
    }

    public void playBlockPlaceSound(BlockPos blockPos, BlockState state)
    {
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        runOnThread(() -> mc.world.playSound(mc.player,
                blockPos,
                blockSoundGroup.getPlaceSound(),
                SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0f) / 2.0f,
                blockSoundGroup.getPitch() * 0.8f));
    }
}

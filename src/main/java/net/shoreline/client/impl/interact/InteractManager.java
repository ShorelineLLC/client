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
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractManager extends NetworkHandler
{
    private final InteractionsModule interactConfig = InteractionsModule.INSTANCE;
    private final AirPlaceModule airPlace = AirPlaceModule.INSTANCE;

    private final ConcurrentMap<Interaction, Long> interactions = new ConcurrentHashMap<>();
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
        interactions.values().removeIf(t -> System.currentTimeMillis() - t > 1000);
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
            for (Interaction interaction : interactions.keySet())
            {
                if (!interaction.getPos().equals(packet.getPos()))
                {
                    continue;
                }

                // Confirm that we succeeded placement serverside
                interaction.setStatus(packet.getState().isOf(interaction.getBlock()) ? InteractStatus.SERVER_CONFIRMED : InteractStatus.SERVER_MISMATCH);
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

        for (Interaction interaction : interactions.keySet())
        {
            if (interaction.getStatus() != InteractStatus.UNCONFIRMED || !interaction.getPos().equals(event.getBlockPos()))
            {
                continue;
            }

            VoxelShape collisionShape = interaction.getBlock().getDefaultState().getCollisionShape(mc.world, event.getBlockPos());
            event.cancel();
            event.setCollisionShape(collisionShape);
            return;
        }
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

        interactions.values().removeIf(t -> System.currentTimeMillis() - t > 1000);
        if (check(blockPos) || isEntityBlocking(blockPos, interaction.getBlock(), true))
        {
            return false;
        }

        boolean result = placeBlockInternal(interaction);
        if (result)
        {
            interactions.put(interaction, System.currentTimeMillis());
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

        Optional<Map.Entry<Interaction, Long>> interact = interactions.entrySet().stream()
                .filter(d -> d.getKey().getPos().equals(blockPos))
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

    private boolean placeBlockInternal(Interaction interaction)
    {
        BlockPos placePos = interaction.getPos();
        BlockState state = interaction.getBlock().getDefaultState();
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
            playBlockPlaceSound(placePos, state);
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

package net.shoreline.client.impl.module.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ObsidianPlacerModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.render.animation.TimeAnimation;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xgraza
 * @since 1.0
 */
public final class SelfTrapModule extends ObsidianPlacerModule
{
    Config<Float> placeRangeConfig = new NumberConfig<>("PlaceRange", "The placement range for surround", 0.0f, 4.0f, 5.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates to block before placing", false);
    Config<Boolean> attackConfig = new BooleanConfig("Attack", "Attacks crystals in the way of surround", true);
    Config<Boolean> centerConfig = new BooleanConfig("Center", "Centers the player before placing blocks", false);
    Config<Boolean> extendConfig = new BooleanConfig("Extend", "Extends surround if the player is not in the center of a block", true);
    Config<Boolean> supportConfig = new BooleanConfig("Support", "Creates a floor for the surround if there is none", false);
    Config<Boolean> headConfig = new BooleanConfig("Head", "If to place a block at your head", true);
    Config<Integer> shiftTicksConfig = new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5);
    Config<Integer> shiftDelayConfig = new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5);
    Config<Boolean> autoDisableConfig = new BooleanConfig("AutoDisable", "Disables after placing the blocks", true);
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders where selftrap is placing blocks", false);
    Config<Integer> fadeTimeConfig = new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false);

    private final Map<BlockPos, TimeAnimation> fadeBoxes = new HashMap<>();
    private final Map<BlockPos, TimeAnimation> fadeLines = new HashMap<>();

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();

    private int blocksPlaced;
    private int shiftDelay;

    private double prevY;

    public SelfTrapModule()
    {
        super("SelfTrap", "Fully surrounds the player with blocks", ModuleCategory.COMBAT, 900);
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null)
        {
            return;
        }
        if (centerConfig.getValue())
        {
            double x = Math.floor(mc.player.getX()) + 0.5;
            double z = Math.floor(mc.player.getZ()) + 0.5;
            Managers.MOVEMENT.setMotionXZ((x - mc.player.getX()) / 2.0, (z - mc.player.getZ()) / 2.0);
        }
        prevY = mc.player.getY();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        disable();
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (mc.player != null && event.getEntity() == mc.player)
        {
            disable();
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        // Do we need this check?? Surround is always highest prio
        blocksPlaced = 0;
        if (autoDisableConfig.getValue() && Math.abs(mc.player.getY() - prevY) > 0.5) {
            disable();
            return;
        }
        BlockPos pos = PlayerUtil.getRoundedBlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        if (shiftDelay < shiftDelayConfig.getValue())
        {
            shiftDelay++;
            return;
        }
        surround = getSelfTrapPositions(pos);
        placements = surround.stream().filter(blockPos -> mc.world.getBlockState(blockPos).isReplaceable()).toList();

        if (placements.isEmpty())
        {
            return;
        }

        final int shiftTicks = shiftTicksConfig.getValue();
        while (blocksPlaced < shiftTicks && !placements.isEmpty())
        {
            if (blocksPlaced >= placements.size())
            {
                break;
            }
            BlockPos targetPos = placements.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            attackPlace(targetPos);
        }
    }

    private void attack(Entity entity)
    {
        Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void attackPlace(BlockPos targetPos)
    {
        if (attackConfig.getValue()) {
            List<Entity> entities = mc.world.getOtherEntities(null, new Box(targetPos)).stream().filter(e -> e instanceof EndCrystalEntity).toList();
            for (Entity entity : entities)
            {
                attack(entity);
            }
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        Managers.INTERACT.placeBlock(targetPos, slot, strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue())
            {
                if (state)
                {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                }
                else
                {
                    Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                }
            }
        });
    }

    public List<BlockPos> getSelfTrapPositions(BlockPos pos) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(pos);
        if (extendConfig.getValue()) {
            for (Direction dir : Direction.values()) {
                if (!dir.getAxis().isHorizontal()) {
                    continue;
                }
                BlockPos pos1 = pos.add(dir.getVector());
                List<Entity> box = mc.world.getOtherEntities(null, new Box(pos1))
                        .stream().filter(e -> !isEntityBlockingTrap(e)).toList();
                if (box.isEmpty()) {
                    continue;
                }
                for (Entity entity : box) {
                    entities.addAll(getAllInBox(entity.getBoundingBox(), pos));
                }
            }
        }
        List<BlockPos> blocks = new CopyOnWriteArrayList<>();
        for (BlockPos epos : entities) {
            for (Direction dir2 : Direction.values()) {
                if (!dir2.getAxis().isHorizontal()) {
                    continue;
                }
                BlockPos pos2 = epos.add(dir2.getVector());
                if (entities.contains(pos2) || blocks.contains(pos2)) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(pos2.toCenterPos());
                if (dist > ((NumberConfig) placeRangeConfig).getValueSq()) {
                    continue;
                }
                blocks.add(pos2);
            }
        }
        if (supportConfig.getValue()) {
            for (BlockPos block : blocks) {
                Direction direction = Managers.INTERACT.getInteractDirection(block, strictDirectionConfig.getValue());
                if (direction == null) {
                    blocks.add(block.down());
                }
            }
        }
        for (BlockPos entityPos : entities) {
            blocks.add(entityPos.down());
        }
        // We now just need to go up one every block pos
        for (final BlockPos blockPos : blocks)
        {
            final BlockPos trapBlockPos = blockPos.up();
            if (entities.contains(blockPos) || entities.contains(trapBlockPos))
            {
                continue;
            }

            // Check if we are still in bounds to place
            final double distance = trapBlockPos.getSquaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (distance > ((NumberConfig<Float>) placeRangeConfig).getValueSq())
            {
                continue;
            }

            blocks.add(trapBlockPos);
        }

        // Sort from furthest from the player first
        blocks.sort(Comparator.comparingDouble((blockPos)
                -> -mc.player.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())));

        if (headConfig.getValue())
        {
            final BlockPos blockPos = PlayerUtil.getRoundedBlockPos(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()).up(2);
            final Direction direction = Managers.INTERACT.getInteractDirection(
                    blockPos, strictDirectionConfig.getValue());
            if (direction != null)
            {
                blocks.add(blockPos);
            }
        }

        Collections.reverse(blocks);
        return blocks;
    }

    private boolean isEntityBlockingTrap(Entity entity)
    {
        return entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity
                || (entity instanceof EndCrystalEntity && attackConfig.getValue());
    }

    /**
     * Returns a {@link List} of all the {@link BlockPos} positions in the
     * given {@link Box} that match the player position level
     *
     * @param box
     * @param pos The player position
     * @return
     */
    public List<BlockPos> getAllInBox(Box box, BlockPos pos)
    {
        final List<BlockPos> intersections = new ArrayList<>();
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++)
        {
            for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++)
            {
                intersections.add(new BlockPos(x, pos.getY(), z));
            }
        }
        return intersections;
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event)
    {
        if (!(event.getEntity() instanceof EndCrystalEntity crystalEntity) || !attackConfig.getValue())
        {
            return;
        }
        for (BlockPos blockPos : surround)
        {
            if (crystalEntity.getBlockPos() == blockPos)
            {
                Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                break;
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            final BlockState state = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos) && state.isReplaceable())
            {
                blocksPlaced++;
                RenderSystem.recordRenderCall(() -> attackPlace(targetPos));
            }
        }
        else if (event.getPacket() instanceof PlaySoundS2CPacket packet
                && packet.getCategory() == SoundCategory.BLOCKS
                && packet.getSound().value() == SoundEvents.ENTITY_GENERIC_EXPLODE)
        {
            BlockPos targetPos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());
            if (surround.contains(targetPos))
            {
                blocksPlaced++;
                RenderSystem.recordRenderCall(() -> attackPlace(targetPos));
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            for (Map.Entry<BlockPos, TimeAnimation> set : fadeBoxes.entrySet())
            {
                set.getValue().setState(false);
                set.getValue().setState(false);
                int alpha = (int) set.getValue().getCurrent();
                Color color = Modules.COLORS.getColor(alpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), color.getRGB());
            }

            for (Map.Entry<BlockPos, TimeAnimation> set : fadeLines.entrySet())
            {
                set.getValue().setState(false);
                int alpha = (int) set.getValue().getCurrent();
                Color color = Modules.COLORS.getColor(alpha);
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, color.getRGB());
            }

            if (placements.isEmpty())
            {
                return;
            }
            for (BlockPos pos : placements)
            {
                TimeAnimation boxAnimation = new TimeAnimation(true, 0, 80, fadeTimeConfig.getValue());
                TimeAnimation lineAnimation = new TimeAnimation(true, 0, 145, fadeTimeConfig.getValue());
                fadeBoxes.put(pos, boxAnimation);
                fadeLines.put(pos, lineAnimation);
            }
        }
    }
}

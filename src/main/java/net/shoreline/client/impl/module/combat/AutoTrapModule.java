package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.ObsidianPlacerModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Shoreline
 * @since 1.0
 */
public final class AutoTrapModule extends ObsidianPlacerModule
{
    private static AutoTrapModule INSTANCE;

    Config<Boolean> multitaskConfig = register(new BooleanConfig("Multitask", "Allows placing while eating", true));
    Config<Float> placeRangeConfig = register(new NumberConfig<>("PlaceRange", "The placement range for trap", 0.0f, 4.0f, 6.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates to block before placing", false));
    Config<Boolean> attackConfig = register(new BooleanConfig("Attack", "Attacks crystals in the way of trap ", true));
    Config<Boolean> extendConfig = register(new BooleanConfig("Extend", "Extends trap if the player is not in the center of a block", true));
    Config<Boolean> supportConfig = register(new BooleanConfig("Support", "Creates a floor for the trap if there is none", false));
    Config<Boolean> headConfig = register(new BooleanConfig("Head", "Place a block at targets head", true));
    Config<Boolean> antiStepConfig = register(new BooleanConfig("PreventStep", "Prevents target from stepping out of the trap", false));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 10));
    Config<Float> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0.0f, 1.0f, 5.0f));
    Config<Boolean> autoDisableConfig = register(new BooleanConfig("AutoDisable", "Disables after placing the blocks", true));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where trap is placing blocks", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private int blocksPlaced;

    public AutoTrapModule()
    {
        super("AutoTrap", "Fully traps enemies with blocks", ModuleCategory.COMBAT, 900);
        INSTANCE = this;
    }

    public static AutoTrapModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        surround.clear();
        placements.clear();
        packets.clear();
        fadeList.clear();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        blocksPlaced = 0;

        if (!multitaskConfig.getValue() && mc.player.isUsingItem())
        {
            surround.clear();
            placements.clear();
            return;
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            surround.clear();
            placements.clear();
            return;
        }
        PlayerEntity trapTarget = getTrapTarget();
        if (trapTarget == null)
        {
            surround.clear();
            placements.clear();
            return;
        }

        BlockPos targetBlockPos = PositionUtil.getRoundedBlockPos(trapTarget.getX(), trapTarget.getY(), trapTarget.getZ());
        surround = getSurround(targetBlockPos, trapTarget);
        if (surround.isEmpty())
        {
            return;
        }
        if (attackConfig.getValue())
        {
            attackBlockingCrystals(surround);
        }
        placements = getPlacementsFromSurround(surround);
        if (placements.isEmpty())
        {
            if (autoDisableConfig.getValue())
            {
                disable();
            }
            return;
        }
        if (supportConfig.getValue())
        {
            for (BlockPos block : new ArrayList<>(placements))
            {
                if (block.getY() > targetBlockPos.getY())
                {
                    continue;
                }
                Direction direction = Managers.INTERACT.getInteractDirectionInternal(block, strictDirectionConfig.getValue());
                if (direction == null)
                {
                    placements.add(block.down());
                }
            }
        }
        placements.sort(Comparator.comparingInt(Vec3i::getY));
        while (blocksPlaced < shiftTicksConfig.getValue())
        {
            if (blocksPlaced >= placements.size())
            {
                break;
            }
            BlockPos targetPos = placements.get(blocksPlaced);
            blocksPlaced++;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPos, slot);
        }

        if (rotateConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handlePackets(packet1);
            }
        }
        else
        {
            handlePackets(event.getPacket());
        }
    }

    private void handlePackets(Packet<?> serverPacket)
    {
        if (serverPacket instanceof BlockUpdateS2CPacket packet)
        {
            final BlockState blockState = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos))
            {
                if (blockState.isReplaceable() && mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, targetPos, ShapeContext.absent()))
                {
                    final int slot = getResistantBlockItem();
                    if (slot == -1)
                    {
                        return;
                    }
                    placeBlock(targetPos, slot);
                }
                else if (BlastResistantBlocks.isBlastResistant(blockState))
                {
                    packets.remove(targetPos);
                }
            }
        }
    }

    private void placeBlock(BlockPos pos, int slot)
    {
        Managers.INTERACT.placeBlock(pos, slot, strictDirectionConfig.getValue(), false, true, (state, angles) ->
        {
            if (rotateConfig.getValue() && state)
            {
                Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
            }
        });
        packets.put(pos, System.currentTimeMillis());
    }

    private PlayerEntity getTrapTarget()
    {
        final List<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        return (PlayerEntity) entities.stream()
                .filter(e -> e instanceof PlayerEntity && e.isAlive() && mc.player != e && !Managers.SOCIAL.isFriend(e.getName()))
                .filter(e -> mc.player.squaredDistanceTo(e) <= ((NumberConfig<Float>) placeRangeConfig).getValueSq())
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)))
                .orElse(null);
    }

    public void attackBlockingCrystals(List<BlockPos> posList)
    {
        for (BlockPos pos : posList)
        {
            Entity crystalEntity = mc.world.getOtherEntities(null, new Box(pos)).stream()
                    .filter(e -> e instanceof EndCrystalEntity).findFirst().orElse(null);
            if (crystalEntity == null)
            {
                continue;
            }
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            return;
        }
    }

    public List<BlockPos> getPlacementsFromSurround(List<BlockPos> surround)
    {
        List<BlockPos> placements = new ArrayList<>();
        for (BlockPos surroundPos : surround)
        {
            Long placed = packets.get(surroundPos);
            if (shiftDelayConfig.getValue() > 0.0f && placed != null && System.currentTimeMillis() - placed < shiftDelayConfig.getValue() * 50.0f)
            {
                continue;
            }
            if (!mc.world.getBlockState(surroundPos).isReplaceable())
            {
                continue;
            }
            double dist = mc.player.squaredDistanceTo(surroundPos.toCenterPos());
            if (dist > ((NumberConfig) placeRangeConfig).getValueSq())
            {
                continue;
            }

            if (mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, surroundPos, ShapeContext.absent()))
            {
                placements.add(surroundPos);
            }
        }
        return placements;
    }

    public List<BlockPos> getSurround(BlockPos playerPos, PlayerEntity player)
    {
        List<BlockPos> surroundBlocks = new ArrayList<>();
        List<BlockPos> playerBlocks = getPlayerBlocks(playerPos, player);
        for (BlockPos pos : playerBlocks)
        {
            for (Direction dir : Direction.values())
            {
                if (!dir.getAxis().isHorizontal())
                {
                    continue;
                }
                BlockPos pos1 = pos.offset(dir);
                if (surroundBlocks.contains(pos1) || playerBlocks.contains(pos1))
                {
                    continue;
                }

                surroundBlocks.add(pos1);
                surroundBlocks.add(pos1.up());
            }
        }
        if (headConfig.getValue())
        {
            boolean support = false;
            final List<BlockPos> headBlocks = new ArrayList<>();
            for (BlockPos pos : playerBlocks)
            {
                BlockPos headPos = pos.offset(Direction.UP, 2);
                if (!mc.world.getBlockState(headPos).isReplaceable())
                {
                    support = true;
                }
                headBlocks.add(headPos);
                if (antiStepConfig.getValue())
                {
                    BlockPos antiStepPos = pos.offset(Direction.UP, 3);
                    headBlocks.add(antiStepPos);
                }
            }
            if (!AirPlaceModule.getInstance().isEnabled())
            {
                BlockPos supportingPos = null;
                double min = Double.MAX_VALUE;
                for (BlockPos pos : surroundBlocks)
                {
                    BlockPos pos1 = pos.offset(Direction.UP, 2);
                    if (!mc.world.getBlockState(pos1).isReplaceable())
                    {
                        support = true;
                        break;
                    }
                    double dist = mc.player.squaredDistanceTo(pos1.toCenterPos());
                    if (dist < min)
                    {
                        supportingPos = pos1;
                        min = dist;
                    }
                }
                if (supportingPos != null && !support)
                {
                    surroundBlocks.add(supportingPos);
                }
            }
            surroundBlocks.addAll(headBlocks);
        }
        return surroundBlocks;
    }

    public List<BlockPos> getPlayerBlocks(BlockPos playerPos, PlayerEntity entity)
    {
        final List<BlockPos> playerBlocks = new ArrayList<>();
        if (extendConfig.getValue())
        {
            playerBlocks.addAll(PositionUtil.getAllInBox(entity.getBoundingBox(), playerPos));
        }
        else
        {
            playerBlocks.add(playerPos);
        }
        return playerBlocks;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
                Color boxColor = ColorsModule.getInstance().getColor(boxAlpha);
                Color lineColor = ColorsModule.getInstance().getColor(lineAlpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }
            RenderBuffers.postRender();

            if (placements.isEmpty())
            {
                return;
            }

            for (BlockPos pos : placements)
            {
                Animation animation = new Animation(true, fadeTimeConfig.getValue());
                fadeList.put(pos, animation);
            }
        }

        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);
    }

    public boolean isPlacing()
    {
        return !placements.isEmpty();
    }
}

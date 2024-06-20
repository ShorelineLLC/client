package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ObsidianPlacerModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author linus
 * @since 1.0
 */
public class SurroundModule extends ObsidianPlacerModule
{
    private static SurroundModule INSTANCE;

    Config<Float> placeRangeConfig = register(new NumberConfig<>("PlaceRange", "The placement range for surround", 0.0f, 4.0f, 6.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates to block before placing", false));
    Config<Boolean> attackConfig = register(new BooleanConfig("Attack", "Attacks crystals in the way of surround", true));
    Config<Boolean> extendConfig = register(new BooleanConfig("Extend", "Extends surround if the player is not in the center of a block", true));
    Config<Boolean> supportConfig = register(new BooleanConfig("Support", "Creates a floor for the surround if there is none", false));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 10));
    Config<Float> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0.0f, 1.0f, 5.0f));
    Config<Float> entityDelayConfig = register(new NumberConfig<>("EntityDelay", "The delay to place when placing on entities", 0.0f, 2.0f, 5.0f));
    Config<Boolean> jumpDisableConfig = register(new BooleanConfig("AutoDisable", "Disables after moving out of the hole", true));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where scaffold is placing blocks", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private final Timer invalidTimer = new CacheTimer();
    private int blocksPlaced;
    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private double prevY;

    public SurroundModule()
    {
        super("Surround", "Surrounds feet with obsidian", ModuleCategory.COMBAT, 950);
        INSTANCE = this;
    }

    public static SurroundModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null)
        {
            return;
        }
        prevY = mc.player.getY();
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
        if (SelfTrapModule.getInstance().isEnabled())
        {
            return;
        }
        blocksPlaced = 0;
        if (jumpDisableConfig.getValue() && Math.abs(mc.player.getY() - prevY) > 0.5)
        {
            disable();
            return;
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        surround = getSurround(mc.player);
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
            return;
        }
        if (supportConfig.getValue())
        {
            for (BlockPos block : new ArrayList<>(placements))
            {
                Direction direction = Managers.INTERACT.getInteractDirection(block, grimConfig.getValue(), strictDirectionConfig.getValue());
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
            double dist = mc.player.squaredDistanceTo(targetPos.toCenterPos());
            if (dist > ((NumberConfig) placeRangeConfig).getValueSq())
            {
                continue;
            }
            blocksPlaced++;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPos, slot);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            final BlockState blockState = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos))
            {
                if (blockState.isReplaceable())
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
        if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                Entity entity = mc.world.getEntityById(id);
                if (entity == null || !(entity instanceof EndCrystalEntity))
                {
                    continue;
                }
                BlockPos targetPos = entity.getBlockPos();
                if (surround.contains(targetPos))
                {
                    final int slot = getResistantBlockItem();
                    if (slot == -1)
                    {
                        return;
                    }
                    placeBlock(targetPos, slot);
                }
            }
        }
    }

    private void placeBlock(BlockPos pos, int slot)
    {
        Managers.INTERACT.placeBlock(pos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, true, (state, angles) ->
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
        packets.put(pos, System.currentTimeMillis());
    }

    public void attackBlockingCrystals(List<BlockPos> posList)
    {
        for (BlockPos blockPos : posList)
        {
            Entity crystalEntity = mc.world.getOtherEntities(null, new Box(blockPos)).stream()
                    .filter(e -> e instanceof EndCrystalEntity).findFirst().orElse(null);
            if (crystalEntity == null)
            {
                continue;
            }
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
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
            List<Entity> invalid = mc.world.getOtherEntities(null, new Box(surroundPos)).stream()
                    .filter(e -> invalidEntity(e)).toList();
            boolean onlyCrystal = invalid.stream().allMatch(e -> e instanceof EndCrystalEntity);
            boolean canPlaceOnCrystal = onlyCrystal && attackConfig.getValue() && invalidTimer.passed(entityDelayConfig.getValue() * 50.0f);
            if (invalid.isEmpty() || canPlaceOnCrystal)
            {
                placements.add(surroundPos);
                if (canPlaceOnCrystal)
                {
                    invalidTimer.reset();
                }
            }
        }
        return placements;
    }

    public List<BlockPos> getSurround(PlayerEntity player)
    {
        List<BlockPos> surroundBlocks = getSurroundNoDown(player);
        List<BlockPos> playerBlocks = getPlayerBlocks(player);
        for (BlockPos playerPos : playerBlocks)
        {
            if (playerPos == player.getBlockPos())
            {
                continue;
            }
            surroundBlocks.add(playerPos.down());
        }
        return surroundBlocks;
    }

    public List<BlockPos> getSurroundNoDown(PlayerEntity player)
    {
        List<BlockPos> surroundBlocks = new ArrayList<>();
        List<BlockPos> playerBlocks = getPlayerBlocks(player);
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
            }
        }
        return surroundBlocks;
    }

    public List<BlockPos> getPlayerBlocks(PlayerEntity entity)
    {
        BlockPos playerPos = PositionUtil.getRoundedBlockPos(entity.getX(), entity.getY(), entity.getZ());
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

    public boolean invalidEntity(Entity entity)
    {
        return !(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (SelfTrapModule.getInstance().isEnabled())
        {
            return;
        }
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (145 * set.getValue().getFactor());
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
}

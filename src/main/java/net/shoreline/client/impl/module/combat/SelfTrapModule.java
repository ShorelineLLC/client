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
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Shorel
 * @since 1.0
 */
public final class SelfTrapModule extends ObsidianPlacerModule
{
    private static SelfTrapModule INSTANCE;

    Config<Float> placeRangeConfig = register(new NumberConfig<>("PlaceRange", "The placement range for trap ", 0.0f, 4.0f, 6.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates to block before placing", false));
    Config<Boolean> attackConfig = register(new BooleanConfig("Attack", "Attacks crystals in the way of trap ", true));
    Config<Boolean> extendConfig = register(new BooleanConfig("Extend", "Extends trap if the player is not in the center of a block", true));
    Config<Boolean> supportConfig = register(new BooleanConfig("Support", "Creates a floor for the trap  if there is none", false));
    Config<Boolean> headConfig = register(new BooleanConfig("Head", "Place a block at your head", true));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5));
    Config<Integer> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5));
    Config<Boolean> autoDisableConfig = register(new BooleanConfig("AutoDisable", "Disables after placing the blocks", true));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where trap is placing blocks", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private int blocksPlaced;
    private double prevY;

    public SelfTrapModule()
    {
        super("SelfTrap", "Fully surrounds the player with blocks", ModuleCategory.COMBAT, 900);
        INSTANCE = this;
    }

    public static SelfTrapModule getInstance()
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
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        blocksPlaced = 0;
        if (autoDisableConfig.getValue() && Math.abs(mc.player.getY() - prevY) > 0.5) {
            disable();
            return;
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        BlockPos pos = PlayerUtil.getRoundedBlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        surround = getSurround(pos);
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
        if (attackConfig.getValue())
        {
            attackBlockingCrystals(placements);
        }
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
            Managers.INTERACT.placeBlock(targetPos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, true, (state, angles) ->
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
            packets.put(targetPos, System.currentTimeMillis());
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            final BlockState blockState = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos) && blockState.isReplaceable())
            {
                blocksPlaced++;
                RenderSystem.recordRenderCall(() -> {
                    final int slot = getResistantBlockItem();
                    if (slot == -1)
                    {
                        return;
                    }
                    Managers.INTERACT.placeBlock(targetPos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, true, (state, angles) ->
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
                });
            }
        }
    }

    public void attackBlockingCrystals(List<BlockPos> surround)
    {
        for (BlockPos blockPos : surround)
        {
            List<Entity> crystalEntities = mc.world.getOtherEntities(null, new Box(blockPos)).stream()
                    .filter(e -> e instanceof EndCrystalEntity).toList();
            if (crystalEntities.isEmpty())
            {
                continue;
            }
            EndCrystalEntity crystal = (EndCrystalEntity) crystalEntities.stream().min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).get();
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            return;
        }
    }

    public List<BlockPos> getPlacementsFromSurround(List<BlockPos> surround)
    {
        List<BlockPos> placements = new ArrayList<>();
        for (BlockPos surroundPos : surround)
        {
            if (packets.containsKey(surroundPos))
            {
                if (System.currentTimeMillis() - packets.get(surroundPos) > shiftDelayConfig.getValue() * 50.0f)
                {
                    packets.remove(surroundPos);
                }
                else
                {
                    continue;
                }
            }
            if (!mc.world.getBlockState(surroundPos).isReplaceable())
            {
                continue;
            }
            List<Entity> invalid = mc.world.getOtherEntities(null, new Box(surroundPos)).stream()
                    .filter(e -> invalidEntity(e)).toList();
            if (!invalid.isEmpty())
            {
                continue;
            }
            placements.add(surroundPos);
        }
        return placements;
    }

    public List<BlockPos> getSurround(BlockPos surroundPos)
    {
        List<BlockPos> surroundBlocks = new ArrayList<>();
        List<BlockPos> playerBlocks = getPlayerBlocks(surroundPos);
        for (BlockPos pos : playerBlocks)
        {
            for (Direction dir : Direction.values())
            {
                if (!dir.getAxis().isHorizontal())
                {
                    continue;
                }
                BlockPos pos1 = pos.add(dir.getVector());
                if (playerBlocks.contains(pos1))
                {
                    continue;
                }
                surroundBlocks.add(pos1);
                surroundBlocks.add(pos1.up());
            }
        }
        return surroundBlocks;
    }

    public List<BlockPos> getPlayerBlocks(BlockPos playerPos)
    {
        final List<BlockPos> playerBlocks = new ArrayList<>();
        if (extendConfig.getValue())
        {
            playerBlocks.addAll(PositionUtil.getAllInBox(mc.player.getBoundingBox(), playerPos));
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
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (80 * set.getValue().getFactor());
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
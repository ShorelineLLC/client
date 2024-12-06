package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;

public class AutoCrawlTrapModule extends ObsidianPlacerModule
{
    Config<Boolean> multitaskConfig = register(new BooleanConfig("Multitask", "Allows placing while eating", true));
    Config<Float> rangeConfig = register(new NumberConfig<>("PlaceRange", "The range to trap enemies", 0.1f, 4.0f, 6.0f));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates to block before placing", false));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 10));
    Config<Float> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0.0f, 1.0f, 5.0f));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders trap placements", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private int blocksPlaced;

    public AutoCrawlTrapModule()
    {
        super("AutoCrawlTrap", "Automatically places blocks to keep enemies in crawl", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable()
    {
        surround.clear();
        placements.clear();
        fadeList.clear();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        blocksPlaced = 0;

        if (!multitaskConfig.getValue() && mc.player.isUsingItem())
        {
            return;
        }

        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        surround = getCrawlTrap();
        if (surround.isEmpty())
        {
            return;
        }
        placements = getPlacementsFromTrap(surround);
        if (placements.isEmpty())
        {
            return;
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

        if (grimConfig.getValue())
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
        if (serverPacket instanceof EntitiesDestroyS2CPacket packet)
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
            if (rotateConfig.getValue() && state)
            {
                Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
            }
        });
        packets.put(pos, System.currentTimeMillis());
    }

    public List<BlockPos> getPlacementsFromTrap(List<BlockPos> surround)
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
            if (dist > ((NumberConfig) rangeConfig).getValueSq())
            {
                continue;
            }
            List<Entity> invalid = mc.world.getOtherEntities(null, new Box(surroundPos)).stream()
                    .filter(e -> invalidEntity(e)).toList();
            if (invalid.isEmpty())
            {
                placements.add(surroundPos);
            }
        }
        return placements;
    }

    public List<BlockPos> getCrawlTrap()
    {
        List<BlockPos> trapBlocks = new ArrayList<>();
        for (Entity entity : mc.world.getEntities())
        {
            if (entity == mc.player || !(entity instanceof PlayerEntity playerEntity)
                    || Managers.SOCIAL.isFriend(entity.getName()))
            {
                continue;
            }
            double dist = mc.player.squaredDistanceTo(playerEntity);
            if (dist > enemyRangeConfig.getValue() * enemyRangeConfig.getValue())
            {
                continue;
            }
            if (playerEntity.isCrawling())
            {
                for (BlockPos targetPos : PositionUtil.getAllInBox(entity.getBoundingBox(), entity.getBlockPos()))
                {
                    trapBlocks.add(targetPos.up());
                }
            }
        }
        return trapBlocks;
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
}

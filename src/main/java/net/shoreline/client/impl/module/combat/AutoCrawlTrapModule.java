package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
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
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;

public class AutoCrawlTrapModule extends ObsidianPlacerModule
{
    private static AutoCrawlTrapModule INSTANCE;

    Config<Float> rangeConfig = register(new NumberConfig<>("PlaceRange", "The range to trap enemies", 0.1f, 4.0f, 6.0f));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f));
    Config<Boolean> downConfig = register(new BooleanConfig("PreventDownwards", "Prevents digging downwards", true));
    Config<Boolean> serverHitboxConfig = register(new BooleanConfig("HitboxSync", "Places on serverside crawling hitboxes", false));
    Config<Boolean> mineIgnoreConfig = register(new BooleanConfig("PreventMine", "Prevents enemies from mining the trap", false));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 10));
    Config<Float> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0.0f, 1.0f, 5.0f));
    Config<Integer> extrapolateTicksConfig = register(new NumberConfig<>("ExtrapolationTicks", "Accounts for motion when calculating enemy positions, not fully accurate.", 0, 0, 10));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders trap placements", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private PlayerEntity target;
    private int blocksPlaced;

    public AutoCrawlTrapModule()
    {
        super("AutoCrawlTrap", "Automatically places blocks to keep enemies in crawl", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static AutoCrawlTrapModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        surround.clear();
        placements.clear();
        fadeList.clear();
        target = null;
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        blocksPlaced = 0;

        if (!multitaskConfig.getValue() && checkMultitask())
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
        target = getClosestPlayer(enemyRangeConfig.getValue());
        if (target == null)
        {
            surround.clear();
            placements.clear();
            return;
        }

        BlockPos targetPos = EntityUtil.getRoundedBlockPos(target);
        surround = getCrawlTrap(target, targetPos);
        if (!canCrawlTrap(target, targetPos) || surround.isEmpty())
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
            BlockPos targetPlacePos = placements.get(blocksPlaced);
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPlacePos, slot);
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
        blocksPlaced++;
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

            final Box surroundBox = new Box(surroundPos);
            List<Entity> invalid = mc.world.getOtherEntities(null, surroundBox).stream().filter(e -> invalidEntity(e)).toList();
            boolean serverCrawling = invalid.stream().allMatch(e -> Managers.HITBOX.isServerCrawling(e)
                    && Managers.HITBOX.getCrawlingBoundingBox(e).intersects(surroundBox));

            if (!mc.world.getBlockState(surroundPos).isReplaceable()
                    && !(serverCrawling && serverHitboxConfig.getValue())
                    && !(Managers.BLOCK.isPassed(surroundPos, 0.7f) && mineIgnoreConfig.getValue()))
            {
                continue;
            }
            double dist = mc.player.squaredDistanceTo(surroundPos.toCenterPos());
            if (dist > ((NumberConfig) rangeConfig).getValueSq())
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

    public List<BlockPos> getCrawlTrap(PlayerEntity entity, BlockPos playerPos)
    {
        final List<BlockPos> crawlTrap = new ArrayList<>();
        crawlTrap.add(playerPos.up());
        if (downConfig.getValue())
        {
            crawlTrap.add(playerPos.down());
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        int ticks = 0;
        while (ticks <= extrapolateTicksConfig.getValue())
        {
            double ox = (x - entity.prevX) * ticks;
            double oz = (z - entity.prevZ) * ticks;
            BlockPos blockPos = BlockPos.ofFloored(x + ox, y, z + oz);
            if (!crawlTrap.contains(blockPos.up()))
            {
                crawlTrap.add(blockPos.up());
            }
            if (downConfig.getValue() && !crawlTrap.contains(blockPos.down()))
            {
                crawlTrap.add(blockPos.down());
            }
            ticks++;
        }
        return crawlTrap;
    }

    public boolean invalidEntity(Entity entity)
    {
        return !(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity) && !(entity instanceof ArrowEntity);
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

    private boolean canCrawlTrap(PlayerEntity player, BlockPos playerPos)
    {
        return player.isOnGround() || !mc.world.getBlockState(playerPos.up()).isReplaceable() || !mc.world.getBlockState(playerPos.up(2)).isReplaceable();
    }

    public boolean isPlacing()
    {
        return !placements.isEmpty();
    }
}

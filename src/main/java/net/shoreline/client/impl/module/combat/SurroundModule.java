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
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author linus
 * @since 1.0
 */
public class SurroundModule extends ObsidianPlacerModule {
    private static SurroundModule INSTANCE;

    Config<Float> placeRangeConfig = register(new NumberConfig<>("PlaceRange", "The placement range for surround", 0.0f, 4.0f, 6.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates to block before placing", false));
    Config<Boolean> attackConfig = register(new BooleanConfig("Attack", "Attacks crystals in the way of surround", true));
    Config<Boolean> centerConfig = register(new BooleanConfig("Center", "Centers the player before placing blocks", false));
    Config<Boolean> extendConfig = register(new BooleanConfig("Extend", "Extends surround if the player is not in the center of a block", true, () -> !centerConfig.getValue()));
    Config<Boolean> supportConfig = register(new BooleanConfig("Support", "Creates a floor for the surround if there is none", false));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5));
    Config<Integer> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5));
    Config<Boolean> jumpDisableConfig = register(new BooleanConfig("AutoDisable", "Disables after moving out of the hole", true));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where scaffold is placing blocks", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private final Map<BlockPos, Animation> fadeList = new HashMap<>();

    private List<BlockPos> surround = new ArrayList<>();
    private List<BlockPos> placements = new ArrayList<>();
    private final Timer attackTimer = new CacheTimer();
    private int inhibitEntity;

    private int blocksPlaced;
    private int shiftDelay;

    private double prevY;

    public SurroundModule() {
        super("Surround", "Surrounds feet with obsidian", ModuleCategory.COMBAT, 950);
        INSTANCE = this;
    }

    public static SurroundModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        surround.clear();
        placements.clear();
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
        if (centerConfig.getValue()) {
            double x = Math.floor(mc.player.getX()) + 0.5;
            double z = Math.floor(mc.player.getZ()) + 0.5;
            Managers.MOVEMENT.setMotionXZ((x - mc.player.getX()) / 2.0, (z - mc.player.getZ()) / 2.0);
        }
        prevY = mc.player.getY();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
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
    public void onPlayerTick(PlayerTickEvent event) {
        if (SelfTrapModule.getInstance().isEnabled()) {
            return;
        }
        // Do we need this check?? Surround is always highest prio
        blocksPlaced = 0;
        if (jumpDisableConfig.getValue() && Math.abs(mc.player.getY() - prevY) > 0.5) {
            disable();
            return;
        }
        if (shiftDelayConfig.getValue() > 0 && shiftDelay < shiftDelayConfig.getValue()) {
            shiftDelay++;
            return;
        }
        // Don't calculate surround positions if no resistant block is in your hotbar
        final int slot = getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        BlockPos pos = PlayerUtil.getRoundedBlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        surround = getSurroundPositions(pos);
        placements = surround.stream().filter(blockPos -> mc.world.getBlockState(blockPos).isReplaceable()).collect(Collectors.toList());
        // We should not be doing anything if we have nothing to place
        if (placements.isEmpty())
        {
            return;
        }
        if (supportConfig.getValue()) {
            for (BlockPos block : new ArrayList<>(placements)) {
                Direction direction = Managers.INTERACT.getInteractDirection(block, grimConfig.getValue(), strictDirectionConfig.getValue());
                if (direction == null) {
                    placements.add(block.down());
                }
            }
        }
        placements.sort(Comparator.comparingInt(Vec3i::getY));
        runAttackBlockingCrystals();
        final int shiftTicks = shiftTicksConfig.getValue();
        while (blocksPlaced < shiftTicks && !placements.isEmpty()) {
            if (blocksPlaced >= placements.size()) {
                break;
            }
            BlockPos targetPos = placements.get(blocksPlaced);
            double dist = mc.player.squaredDistanceTo(targetPos.toCenterPos());
            if (dist > ((NumberConfig) placeRangeConfig).getValueSq()) {
                continue;
            }
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            place(targetPos);
        }
    }

    private void runAttackBlockingCrystals()
    {
        if (attackConfig.getValue() && attackTimer.passed(AutoCrystalModule.getInstance().getBreakDelay()))
        {
            for (BlockPos block : placements)
            {
                List<Entity> crystalEntities = mc.world.getOtherEntities(null, new Box(block)).stream()
                        .filter(e -> e instanceof EndCrystalEntity).toList();
                for (Entity entity : crystalEntities)
                {
                    if (attack(entity))
                    {
                        attackTimer.reset();
                        return;
                    }
                }
            }
        }
    }

    private boolean attack(Entity entity)
    {
        inhibitEntity = entity.getId();
        Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        return true;
    }

    private void place(BlockPos targetPos) {
        List<Entity> box = mc.world.getOtherEntities(null, new Box(targetPos))
                .stream().filter(e -> !canPlaceOnEntity(e)).toList();
        if (!box.isEmpty())
        {
            return;
        }
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
    }

    public List<BlockPos> getSurroundPositions(BlockPos pos) {
        List<BlockPos> entities = getSurroundEntities(pos);
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
                blocks.add(pos2);
            }
        }
        for (BlockPos entityPos : entities) {
            if (entityPos == pos) {
                continue;
            }
            blocks.add(entityPos.down());
        }
        return blocks;
    }

    public List<BlockPos> getSurroundEntities(Entity entity) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(entity.getBlockPos());
        if (extendConfig.getValue()) {
            for (Direction dir : Direction.values()) {
                if (!dir.getAxis().isHorizontal()) {
                    continue;
                }
                entities.addAll(PositionUtil.getAllInBox(entity.getBoundingBox(), entity.getBlockPos()));
            }
        }
        return entities;
    }

    public List<BlockPos> getSurroundEntities(BlockPos pos) {
        List<BlockPos> entities = new LinkedList<>();
        entities.add(pos);
        if (extendConfig.getValue()) {
            for (Direction dir : Direction.values()) {
                if (!dir.getAxis().isHorizontal()) {
                    continue;
                }
                BlockPos pos1 = pos.add(dir.getVector());
                List<Entity> box = mc.world.getOtherEntities(null, new Box(pos1))
                        .stream().filter(e -> !canPlaceOnEntity(e)).toList();
                if (box.isEmpty()) {
                    continue;
                }
                for (Entity entity : box) {
                    entities.addAll(PositionUtil.getAllInBox(entity.getBoundingBox(), pos));
                }
            }
        }
        return entities;
    }

    public List<BlockPos> getEntitySurroundNoSupport(Entity entity) {
        List<BlockPos> entities = getSurroundEntities(entity);
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
                blocks.add(pos2);
            }
        }
        return blocks;
    }

    public boolean canPlaceOnEntity(Entity entity) {
        return entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity
                || (entity instanceof EndCrystalEntity && attackConfig.getValue());
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.player == null || mc.world == null || SelfTrapModule.getInstance().isEnabled()) {
            return;
        }
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            final BlockState state = packet.getState();
            final BlockPos targetPos = packet.getPos();
            if (surround.contains(targetPos) && state.isReplaceable()) {
                blocksPlaced++;
                RenderSystem.recordRenderCall(() -> place(targetPos));
            }
        }
        else if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet) {
            for (int id : packet.getEntityIds()) {
                Entity entity = mc.world.getEntityById(id);
                if (entity instanceof EndCrystalEntity && surround.contains(entity.getBlockPos())) {
                    blocksPlaced++;
                    RenderSystem.recordRenderCall(() -> place(entity.getBlockPos()));
                }
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (SelfTrapModule.getInstance().isEnabled()) {
            return;
        }
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

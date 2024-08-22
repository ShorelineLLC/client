package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.*;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.impl.module.combat.SurroundModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.collection.FirstOutQueue;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.client.util.world.ExplosionUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

// Do not look at this code

/**
 * @author Shoreline
 * @since 1.0
 */
public class AutoMineModule extends RotationModule
{

    Config<Boolean> multitaskConfig = register(new BooleanConfig("Multitask", "Allows mining while using items", false));
    Config<Boolean> autoConfig = register(new BooleanConfig("Auto", "Automatically mines nearby players feet", false));
    Config<Selection> selectionConfig = register(new EnumConfig<>("Selection", "The selection of blocks mine", Selection.ALL, Selection.values(), () -> autoConfig.getValue()));
    Config<List<Block>> whitelistConfig = register(new BlockListConfig<>("Whitelist", "Valid block whitelist", Blocks.OBSIDIAN, Blocks.ENDER_CHEST));
    Config<List<Block>> blacklistConfig = register(new BlockListConfig<>("Blacklist", "Valid block blacklist", Blocks.SHULKER_BOX));
    Config<Boolean> autoRemineConfig = register(new BooleanConfig("AutoRemine", "Automatically remines mined blocks", true, () -> autoConfig.getValue()));
    Config<Boolean> strictDirectionConfig = register(new BooleanConfig("StrictDirection", "Only mines on visible faces", false, () -> autoConfig.getValue()));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> autoConfig.getValue()));
    Config<Boolean> doubleBreakConfig = register(new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 6.0f));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f));
    Config<Swap> swapConfig = register(new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values()));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates when mining the block", true));
    Config<Boolean> switchResetConfig = register(new BooleanConfig("SwitchReset", "Resets mining after switching items", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    Config<Boolean> instantConfig = register(new BooleanConfig("Instant", "Instant remines mined blocks", true));
    // Config<Boolean> headConfig = register(new BooleanConfig("Head", "Attempts to mine players head blocks", false));
    Config<Boolean> crawlingConfig = register(new BooleanConfig("AntiCrawl", "Attempts to stop player from crawling", false));
    Config<Color> colorConfig = register(new ColorConfig("MineColor", "The mine render color", Color.RED, false, false));
    Config<Color> colorDoneConfig = register(new ColorConfig("DoneColor", "The done render color", Color.GREEN, false, false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private final Map<MiningData, Animation> fadeList = new HashMap<>();
    private FirstOutQueue<MiningData> miningQueue = new FirstOutQueue<>(2);
    private final List<BlockPos> packetMines = new ArrayList<>();
    private long lastBreak;
    private boolean manualOverride;
    private final Timer stopMiningTimer = new CacheTimer();

    public AutoMineModule()
    {
        super("AutoMine", "Automatically mines blocks", ModuleCategory.WORLD, 900);
    }

    @Override
    public String getModuleData()
    {
        MiningData miningData = miningQueue.peek();
        if (miningData != null)
        {
            return String.format("%.1f", Math.min(miningData.getBlockDamage(), 1.0f));
        }
        return super.getModuleData();
    }

    @Override
    protected void onDisable()
    {
        miningQueue.clear();
        fadeList.clear();
        manualOverride = false;
        Managers.INVENTORY.syncToClient();
    }

    @Override
    public void onEnable()
    {
        if (doubleBreakConfig.getValue())
        {
            miningQueue = new FirstOutQueue<>(2);
        }
        else
        {
            miningQueue = new FirstOutQueue<>(1);
        }
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }
        if (doubleBreakConfig.getValue())
        {
            clearMiningFloor();
        }

        if (autoConfig.getValue() && !manualOverride)
        {
            if (mc.player.isCrawling() && crawlingConfig.getValue() && getCrawlingMine() != null)
            {
                BlockPos crawlingMine = getCrawlingMine();
                miningQueue.clear();
                manualOverride = true;
                queueMiningData(new AutoMiningData(crawlingMine, Direction.DOWN));
            }
            else
            {
                PlayerEntity playerTarget = null;
                double minDistance = Float.MAX_VALUE;
                for (PlayerEntity entity : mc.world.getPlayers())
                {
                    if (entity instanceof ClientPlayerEntity || Managers.SOCIAL.isFriend(entity.getName()))
                    {
                        continue;
                    }
                    double dist = mc.player.distanceTo(entity);
                    if (dist > enemyRangeConfig.getValue())
                    {
                        continue;
                    }
                    if (dist < minDistance)
                    {
                        minDistance = dist;
                        playerTarget = entity;
                    }
                }
                if (playerTarget != null)
                {
                    MiningData miningData = null;
                    MiningData miningDataLast = null;
                    if (!miningQueue.isEmpty())
                    {
                        miningData = miningQueue.getLast();
                        if (miningQueue.size() > 1)
                        {
                            miningDataLast = miningQueue.getFirst();
                        }
                    }
                    List<AutoMineCalc> phasePositions = getPhasePosition(playerTarget);
                    PriorityQueue<AutoMineCalc> miningPositions = getMiningPosition(playerTarget);
                    PriorityQueue<AutoMineCalc> miningPositions2 = miningPositions.stream().filter(c -> !mc.world.isAir(c.pos())).collect(Collectors.toCollection(PriorityQueue::new));
                    if (doubleBreakConfig.getValue())
                    {
                        AutoMineCalc miningPos;
                        AutoMineCalc miningPos2;
                        boolean miningPhasePos = !phasePositions.isEmpty();
                        if (miningPhasePos)
                        {
                            miningPos2 = phasePositions.remove(0);
                            miningPos = !phasePositions.isEmpty() ? phasePositions.remove(0) : autoRemineConfig.getValue() && miningPos2 == null ? miningPositions.peek() : miningPositions2.peek();
                        }
                        else
                        {
                            miningPos = autoRemineConfig.getValue() ? miningPositions.peek() : miningPositions2.peek();
                            boolean b2 = miningPos != null && !mc.world.isAir(miningPos.pos());
                            if (b2)
                            {
                                miningPositions2.poll();
                            }
                            miningPos2 = b2 ? miningPositions2.peek() : miningPositions.peek();
                        }
                        boolean mine2 = miningPos2 != null;
                        boolean mine1 = miningPos != null;
                        MiningData instantMine = miningDataLast == null ? miningData : miningDataLast;
                        if (mine1 && mine2)
                        {
                            boolean instantMineIncorrect = instantMine == null || !instantMine.getPos().equals(miningPos.pos()) && miningQueue.size() < 2;
                            if (miningPhasePos && miningQueue.size() < 2 || instantMineIncorrect)
                            {
                                if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.getBlockState(miningData.getPos()).isReplaceable() && autoRemineConfig.getValue())
                                {
                                    stopMining(miningData);
                                    if (!miningData.hasAttemptedBreak())
                                    {
                                        miningData.setAttemptedBreak(true);
                                    }
                                }
                                else
                                {
                                    boolean full2 = !mc.world.isAir(miningPos2.pos());
                                    boolean full = !mc.world.isAir(miningPos.pos());
                                    if (full && full2)
                                    {
                                        miningQueue.clear();
                                    }
                                    if (full2)
                                    {
                                        queueMiningData(new AutoMiningData(miningPos2.pos(),
                                                strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(miningPos2.pos()) : Direction.UP));
                                    }
                                    if (full)
                                    {
                                        queueMiningData(new AutoMiningData(miningPos.pos(),
                                                strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(miningPos.pos()) : Direction.UP));
                                    }
                                }
                            }
                        }
                        else if (mine1)
                        {
                            boolean instantMineIncorrect = instantMine == null || !instantMine.getPos().equals(miningPos.pos());
                            if (instantMineIncorrect)
                            {
                                // If we are re-mining, bypass throttle check below
                                if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.getBlockState(miningData.getPos()).isReplaceable() && autoRemineConfig.getValue())
                                {
                                    stopMining(miningData);
                                    if (!miningData.hasAttemptedBreak())
                                    {
                                        miningData.setAttemptedBreak(true);
                                    }
                                }
                                else
                                {
                                    if (!mc.world.isAir(miningPos.pos()))
                                    {
                                        queueMiningData(new AutoMiningData(miningPos.pos(),
                                                strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(miningPos.pos()) : Direction.UP));
                                    }
                                }
                            }
                        }
                        else if (mine2)
                        {
                            boolean instantMineIncorrect = instantMine == null || !instantMine.getPos().equals(miningPos2.pos());
                            if (miningPhasePos && miningQueue.size() < 2 || instantMineIncorrect)
                            {
                                // If we are re-mining, bypass throttle check below
                                if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.getBlockState(miningData.getPos()).isReplaceable() && autoRemineConfig.getValue())
                                {
                                    stopMining(miningData);
                                    if (!miningData.hasAttemptedBreak())
                                    {
                                        miningData.setAttemptedBreak(true);
                                    }
                                }
                                else
                                {
                                    queueMiningData(new AutoMiningData(miningPos2.pos(),
                                            strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(miningPos2.pos()) : Direction.UP));
                                }
                            }
                        }
                    }
                    else
                    {
                        AutoMineCalc miningPos = !phasePositions.isEmpty() ? phasePositions.remove(0) : autoRemineConfig.getValue() ? miningPositions.peek() : miningPositions2.peek();
                        if (miningPos != null)
                        {
                            boolean instantMineIncorrect = miningData != null && miningData.getPos() != miningPos.pos();
                            if (instantMineIncorrect || miningQueue.isEmpty())
                            {
                                // If we are re-mining, bypass throttle check below
                                if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.getBlockState(miningData.getPos()).isReplaceable() && autoRemineConfig.getValue())
                                {
                                    stopMining(miningData);
                                    if (!miningData.hasAttemptedBreak())
                                    {
                                        miningData.setAttemptedBreak(true);
                                    }
                                }
                                else if (!mc.world.isAir(miningPos.pos()) && !isBlockDelayGrim())
                                {
                                    queueMiningData(new AutoMiningData(miningPos.pos(),
                                            strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(miningPos.pos()) : Direction.UP));
                                }
                            }
                        }
                    }
                }
                else
                {
                    miningQueue.removeIf(d -> d instanceof AutoMiningData);
                }
            }
        }
        if (miningQueue.isEmpty())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (data.getState().isAir())
            {
                data.resetBreakTime();
            }
            if (isDataPacketMine(data) && (data.getState().isAir() || data.hasAttemptedBreak() && data.passedAttemptedBreakTime(1000)))
            {
                Managers.INVENTORY.syncToClient();
                miningQueue.remove(data);
                continue;
            }
            final float damageDelta = SpeedmineModule.getInstance().calcBlockBreakingDelta(
                    data.getState(), mc.world, data.getPos());
            data.damage(damageDelta);
            if (isDataPacketMine(data) && data.getBlockDamage() >= 1.0f && data.getSlot() != -1)
            {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue())
                {
                    return;
                }
                Managers.INVENTORY.setSlot(data.getSlot());
                if (!data.hasAttemptedBreak())
                {
                    data.setAttemptedBreak(true);
                }
            }
        }
        MiningData miningData2 = miningQueue.getFirst();
        final double distance = mc.player.getEyePos().squaredDistanceTo(miningData2.getPos().toCenterPos());
        if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq())
        {
            // abortMining(miningData);
            miningQueue.remove(miningData2);
            return;
        }
        if (miningData2.getState().isAir())
        {
            // Once we broke the block that overrode that the auto city, we can allow the module
            // to auto mine "city" blocks
            if (manualOverride)
            {
                manualOverride = false;
                miningQueue.remove(miningData2);
                return;
            }
            if (instantConfig.getValue())
            {
                if (miningData2 instanceof AutoMiningData && !autoRemineConfig.getValue())
                {
                    miningQueue.remove(miningData2);
                    return;
                }
                miningData2.setInstantRemine();
                miningData2.setDamage(1.0f);
            }
            else
            {
                miningData2.resetDamage();
            }
            return;
        }
        // Something went wrong, remove and remine
        if (miningData2.getBlockDamage() >= speedConfig.getValue() && miningData2.hasAttemptedBreak() && miningData2.passedAttemptedBreakTime(1000))
        {
            abortMining(miningData2);
            miningQueue.remove(miningData2);
        }
        if (miningData2.getBlockDamage() >= speedConfig.getValue() || miningData2.isInstantRemine())
        {
            if (mc.player.isUsingItem() && !multitaskConfig.getValue())
            {
                return;
            }
            if (instantConfig.getValue() || stopMiningTimer.passed(500))
            {
                stopMining(miningData2);
                if (!miningData2.hasAttemptedBreak())
                {
                    miningData2.setAttemptedBreak(true);
                }
                stopMiningTimer.reset();
            }
        }
    }

    @EventListener
    public void onAttackBlock(final AttackBlockEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }
        event.cancel();
        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || event.getState().isAir())
        {
            return;
        }
        int queueSize = miningQueue.size();
        if (queueSize == 0)
        {
            startManualMine(event.getPos(), event.getDirection());
        }
        else if (queueSize == 1)
        {
            MiningData data = miningQueue.getFirst();
            if (data.getPos().equals(event.getPos()))
            {
                // abortMining(miningData);
                return;
            }
            if (data instanceof AutoMiningData)
            {
                miningQueue.clear();
                manualOverride = true;
            }
            startManualMine(event.getPos(), event.getDirection());
        }
        else if (queueSize == 2)
        {
            MiningData data1 = miningQueue.getFirst();
            MiningData data2 = miningQueue.getLast();
            if (data1.getPos().equals(event.getPos()) || data2.getPos().equals(event.getPos()))
            {
                // abortMining(miningData);
                return;
            }
            if (data1 instanceof AutoMiningData && data2 instanceof AutoMiningData)
            {
                miningQueue.remove();
                manualOverride = true;
            }
            startManualMine(event.getPos(), event.getDirection());
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue())
        {
            for (MiningData data : miningQueue)
            {
                data.resetDamage();
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            if (packet.getState().isAir())
            {
                for (MiningData data : miningQueue)
                {
                    if (data.hasAttemptedBreak() && data.getPos().equals(packet.getPos()))
                    {
                        data.setAttemptedBreak(false);
                    }
                }
            }
            else if (!instantConfig.getValue())
            {
                for (MiningData data : miningQueue)
                {
                    if (data.getPos().equals(packet.getPos()))
                    {
                        startMining(data, false);
                    }
                }
            }
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.POST && event.getConfig() == doubleBreakConfig)
        {
            if (doubleBreakConfig.getValue())
            {
                miningQueue = new FirstOutQueue<>(2);
            }
            else
            {
                miningQueue = new FirstOutQueue<>(1);
            }
        }
    }

    @EventListener
    public void onRenderWorld(final RenderWorldEvent event)
    {
        if (mc.player.isCreative())
        {
            return;
        }
        RenderBuffers.preRender();
        for (Map.Entry<MiningData, Animation> set : fadeList.entrySet())
        {
            MiningData data = set.getKey();
            set.getValue().setState(false);
            int boxAlpha = (int) (40 * set.getValue().getFactor());
            int lineAlpha = (int) (145 * set.getValue().getFactor());
            int boxColor = data.getBlockDamage() >= 0.95f ? ((ColorConfig) colorDoneConfig).getRgb(boxAlpha) : ((ColorConfig) colorConfig).getRgb(boxAlpha);
            int lineColor = data.getBlockDamage() >= 0.95f ? ((ColorConfig) colorDoneConfig).getRgb(lineAlpha) : ((ColorConfig) colorConfig).getRgb(lineAlpha);
            BlockPos mining = data.getPos();
            VoxelShape outlineShape = VoxelShapes.fullCube();
            if (!data.isInstantRemine())
            {
                outlineShape = data.getState().getOutlineShape(mc.world, mining);
                outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                    mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                    mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float total = isDataPacketMine(data) ? 1.0f : speedConfig.getValue();
            float scale = data.isInstantRemine() ? 1.0f : MathHelper.clamp((data.getBlockDamage() + (data.getBlockDamage() - data.getLastDamage()) * event.getTickDelta()) / total, 0.0f, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
            RenderManager.renderBox(event.getMatrices(), scaled, boxColor);
            RenderManager.renderBoundingBox(event.getMatrices(), scaled, 1.5f, lineColor);
        }
        for (MiningData data : miningQueue)
        {
            Animation animation = new Animation(true, fadeTimeConfig.getValue());
            fadeList.put(data, animation);
        }
        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);
        RenderBuffers.postRender();
    }

    private void startManualMine(BlockPos pos, Direction direction)
    {
        if (isBlockDelayGrim())
        {
            return;
        }
        queueMiningData(new MiningData(pos, direction));
    }

    private void queueMiningData(MiningData data)
    {
        if (miningQueue.stream().anyMatch(p1 -> data.getPos().equals(p1.getPos())))
        {
            return;
        }
        boolean floor = isMiningFloor(data);
        if (floor && !miningQueue.isEmpty() || miningQueue.stream().anyMatch(d -> d.isFloor()))
        {
            miningQueue.clear();
        }
        if (data.getState().isAir())
        {
            return;
        }
        if (startMining(data, floor))
        {
            miningQueue.addFirst(data);
        }
    }

    private List<AutoMineCalc> getPhasePosition(PlayerEntity entity)
    {
        List<AutoMineCalc> phasePositions = new ArrayList<>();
        List<BlockPos> entityIntersections = PositionUtil.getAllInBox(entity.getBoundingBox(), entity.getBlockPos());
        for (BlockPos blockPos : entityIntersections)
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                continue;
            }
            BlockState state = mc.world.getBlockState(blockPos);
            if (!state.isReplaceable() && validAutoMineBlock(state.getBlock()))
            {
                phasePositions.add(new AutoMineCalc(blockPos, -1.0, true));
            }
        }
        return phasePositions;
    }

    private PriorityQueue<AutoMineCalc> getMiningPosition(PlayerEntity entity)
    {
        PriorityQueue<AutoMineCalc> miningPositions = new PriorityQueue<>();
        List<BlockPos> surroundBlocks = SurroundModule.getInstance().getSurroundNoDown(entity);
        for (BlockPos blockPos : surroundBlocks)
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                continue;
            }
            double damage = ExplosionUtil.getDamageTo(entity, blockPos.toCenterPos(), ExplosionUtil.IgnoreTerrain.ALL);
            // Check surrounding positions
            miningPositions.add(new AutoMineCalc(blockPos, -damage, false));
        }
        miningPositions.removeIf(c -> BlastResistantBlocks.isUnbreakable(c.pos()));
        miningPositions.removeAll(getPhasePosition(mc.player));
//        if (headConfig.getValue())
//        {
//            BlockPos headPos = entity.getBlockPos().up(2);
//            if (miningPositions.isEmpty() && !mc.world.getBlockState(headPos).isReplaceable())
//            {
//                miningPositions.add(new AutoMineCalc(headPos, Double.MAX_VALUE, false));
//            }
//        }
        return miningPositions;
    }

    private BlockPos getCrawlingMine()
    {
        BlockPos crawlingPos = mc.player.getBlockPos();
        if (!BlastResistantBlocks.isUnbreakable(crawlingPos.up()) && !mc.world.isAir(crawlingPos.up()))
        {
            return crawlingPos.up();
        }
        return null;
    }

    private record AutoMineCalc(BlockPos pos, double entityDamage, boolean phase) implements Comparable<AutoMineCalc>
    {
        @Override
        public int compareTo(@NotNull AutoMineCalc o)
        {
            return Double.compare(-entityDamage(), -o.entityDamage());
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof AutoMineCalc calc)
            {
                return calc.pos().equals(pos);
            }
            return false;
        }
    }

    public boolean isMiningFloor(MiningData data)
    {
        for (BlockPos pos : PositionUtil.getAllInBox(mc.player.getBoundingBox(), mc.player.getBlockPos()))
        {
            if (data.getPos().equals(pos.down()))
            {
                return true;
            }
        }
        return false;
    }

    public void clearMiningFloor()
    {
        for (BlockPos pos : PositionUtil.getAllInBox(mc.player.getBoundingBox(), mc.player.getBlockPos()))
        {
            BlockPos miningFloor = pos.down();
            if (packetMines.contains(miningFloor) && !mc.world.isAir(miningFloor))
            {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos.down(), Direction.UP));
                packetMines.remove(pos.down());
                miningQueue.removeIf(d -> d.getPos().equals(pos.down()));
            }
        }
    }

    private boolean startMining(MiningData data, boolean floor)
    {
        if (data.isStarted())
        {
            return false;
        }
        if (doubleBreakConfig.getValue() && !floor)
        {
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
            if (grimConfig.getValue())
            {
                Managers.INVENTORY.setSlot(data.getSlot());
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            }
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            packetMines.add(data.getPos());
        }
        else
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            if (floor)
            {
                data.setFloorMine();
                manualOverride = true;
            }
        }
        data.setStarted();
        Managers.INVENTORY.syncToClient();
        return true;
    }

    private void abortMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir() || data.isInstantRemine())
        {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.INVENTORY.syncToClient();
    }

    private void stopMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir())
        {
            return;
        }
        if (rotateConfig.getValue())
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            if (grimConfig.getValue())
            {
                setRotationSilent(rotations[0], rotations[1]);
            }
            else
            {
                setRotation(rotations[0], rotations[1]);
            }
        }
        int slot = data.getSlot();
        boolean canSwap = slot != -1;
        if (canSwap)
        {
            swapTo(slot);
        }
        stopMiningInternal(data);
        lastBreak = System.currentTimeMillis();
        if (canSwap)
        {
            swapSync(slot);
        }
        if (rotateConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync(true);
        }
    }

    private void swapTo(int slot)
    {
        switch (swapConfig.getValue())
        {
            case NORMAL -> Managers.INVENTORY.setClientSlot(slot);
            case SILENT -> Managers.INVENTORY.setSlot(slot);
            case SILENT_ALT -> Managers.INVENTORY.setSlotAlt(slot);
        }
    }

    private void swapSync(int slot)
    {
        switch (swapConfig.getValue())
        {
            case SILENT -> Managers.INVENTORY.syncToClient();
            case SILENT_ALT -> Managers.INVENTORY.setSlotAlt(slot);
        }
    }

    private void stopMiningInternal(MiningData data)
    {
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        if (grimConfig.getValue())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos().up(500), data.getDirection()));
        }
    }

    // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L80
    public boolean isBlockDelayGrim()
    {
        return System.currentTimeMillis() - lastBreak <= 280 && grimConfig.getValue();
    }

    private boolean isDataPacketMine(MiningData data)
    {
        return miningQueue.size() == 2 && data == miningQueue.getLast();
    }

    private boolean validAutoMineBlock(Block block)
    {
        return switch (selectionConfig.getValue())
        {
            case WHITELIST -> ((BlockListConfig<?>) whitelistConfig).contains(block);
            case BLACKLIST -> !((BlockListConfig<?>) blacklistConfig).contains(block);
            case ALL -> true;
        };
    }

    public class AutoMiningData extends MiningData
    {
        public AutoMiningData(BlockPos pos, Direction direction)
        {
            super(pos, direction);
        }
    }

    public static class MiningData
    {
        private boolean attemptedBreak;
        private long breakTime;
        private final BlockPos pos;
        private final Direction direction;
        private float lastDamage;
        private float blockDamage;
        private boolean instantRemine;
        private boolean floor;
        private boolean started;

        public MiningData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
        }

        public void setAttemptedBreak(boolean attemptedBreak)
        {
            this.attemptedBreak = attemptedBreak;
            if (attemptedBreak)
            {
                resetBreakTime();
            }
        }

        public void resetBreakTime()
        {
            breakTime = System.currentTimeMillis();
        }

        public boolean hasAttemptedBreak()
        {
            return attemptedBreak;
        }

        public boolean passedAttemptedBreakTime(long time)
        {
            return System.currentTimeMillis() - breakTime >= time;
        }

        public boolean isInstantRemine()
        {
            return instantRemine;
        }

        public void setInstantRemine()
        {
            this.instantRemine = true;
        }

        public float damage(final float dmg)
        {
            lastDamage = blockDamage;
            blockDamage += dmg;
            return blockDamage;
        }

        public void setDamage(float blockDamage)
        {
            this.blockDamage = blockDamage;
        }

        public void resetDamage()
        {
            instantRemine = false;
            started = false;
            blockDamage = 0.0f;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public int getSlot()
        {
            return AutoToolModule.getInstance().getBestToolNoFallback(getState());
        }

        public BlockState getState()
        {
            return mc.world.getBlockState(pos);
        }

        public void setFloorMine()
        {
            this.floor = true;
        }

        public boolean isFloor()
        {
            return floor;
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }

        public boolean isStarted()
        {
            return started;
        }

        public void setStarted()
        {
            this.started = true;
        }
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }
}
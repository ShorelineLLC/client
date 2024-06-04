package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
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
import net.shoreline.eventbus.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

// Do not look at this code

/**
 * @author Shoreline
 * @since 1.0
 */
public class AutoMineModule extends RotationModule
{

    Config<Boolean> multitaskConfig = register(new BooleanConfig("Multitask", "Allows mining while using items", false));
    Config<Boolean> autoConfig = register(new BooleanConfig("Auto", "Automatically mines nearby players feet", false));
    Config<Boolean> autoRemineConfig = register(new BooleanConfig("AutoRemine", "Automatically remines mined blocks", true, () -> autoConfig.getValue()));
    Config<Boolean> strictDirectionConfig = register(new BooleanConfig("StrictDirection", "Only mines on visible faces", false, () -> autoConfig.getValue()));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> autoConfig.getValue()));
    Config<Boolean> doubleBreakConfig = register(new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 6.0f));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates when mining the block", true));
    Config<Boolean> switchResetConfig = register(new BooleanConfig("SwitchReset", "Resets mining after switching items", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    Config<Boolean> instantConfig = register(new BooleanConfig("Instant", "Instant remines mined blocks", true));
    Config<AntiCrawling> crawlingConfig = register(new EnumConfig<>("AntiCrawl", "Attempts to prevent player from crawling", AntiCrawling.OFF, AntiCrawling.values()));
    Config<Color> colorConfig = register(new ColorConfig("MineColor", "The mine render color", Color.RED, false, false));
    Config<Color> colorDoneConfig = register(new ColorConfig("DoneColor", "The done render color", Color.GREEN, false, false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));
    //
    private final Map<MiningData, Animation> fadeList = new HashMap<>();
    private FirstOutQueue<MiningData> miningQueue = new FirstOutQueue<>(2);
    private long lastBreak;
    private boolean manualOverride;

    public AutoMineModule()
    {
        super("AutoMine", "Automatically mines blocks", ModuleCategory.WORLD, 900);
    }

    @Override
    public String getModuleData()
    {
        if (miningQueue.peek() != null)
        {
            return String.format("%.1f", Math.min(miningQueue.peek().getBlockDamage(), 1.0f));
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
        MiningData miningData = null;
        MiningData miningDataLast = null;
        if (!miningQueue.isEmpty())
        {
            miningData = miningQueue.getFirst();
            miningDataLast = miningQueue.getLast();
        }
        if (autoConfig.getValue() && !manualOverride)
        {
            if (mc.player.isCrawling() && crawlingConfig.getValue() != AntiCrawling.OFF && getCrawlingMine() != null)
            {
                BlockPos crawlingMine = getCrawlingMine();
                miningQueue.clear();
                manualOverride = true;
                MiningData data = new AutoMiningData(crawlingMine, Direction.UP);
                queueMiningData(data);
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
                    PriorityQueue<AutoMineCalc> miningPositions = getMiningPosition(playerTarget);
                    PriorityQueue<AutoMineCalc> miningPositionsNoAir = getNoAir(miningPositions);
                    PriorityQueue<AutoMineCalc> cityPositions = autoRemineConfig.getValue() ? miningPositions : miningPositionsNoAir;
                    if (cityPositions.isEmpty())
                    {
                        return;
                    }
                    if (doubleBreakConfig.getValue() && (miningData == null || (mc.world.getBlockState(miningData.getPos()).isAir() && mc.world.getBlockState(miningDataLast.getPos()).isAir())))
                    {
                        final AutoMineCalc cityBlockPos = cityPositions.peek();
                        if (cityBlockPos != null)
                        {
                            miningPositionsNoAir.removeIf(p -> p.pos().equals(cityBlockPos.pos()));
                            final AutoMineCalc cityBlockPos2 = miningPositionsNoAir.peek();
                            // If we are re-mining, bypass throttle check below
                            if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.getBlockState(miningData.getPos()).isReplaceable() && autoRemineConfig.getValue())
                            {
                                stopMining(miningData);
                                if (!miningData.hasAttemptedBreak())
                                {
                                    miningData.setAttemptedBreak();
                                }
                            }
                            else if (!mc.world.isAir(cityBlockPos.pos()) && !isBlockDelayGrim())
                            {
                                miningQueue.clear();
                                MiningData data = new AutoMiningData(cityBlockPos.pos(),
                                        strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityBlockPos.pos()) : Direction.UP);
                                queueMiningData(data);
                                if (cityBlockPos2 != null && !mc.world.isAir(cityBlockPos2.pos()))
                                {
                                    MiningData data2 = new AutoMiningData(cityBlockPos2.pos(),
                                            strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityBlockPos2.pos()) : Direction.UP);
                                    queueMiningData(data2);
                                }
                            }
                        }
                    }
                    else if (miningData == null || mc.world.getBlockState(miningData.getPos()).isAir())
                    {
                        final AutoMineCalc cityBlockPos = cityPositions.poll();
                        if (cityBlockPos != null)
                        {
                            // If we are re-mining, bypass throttle check below
                            if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.getBlockState(miningData.getPos()).isReplaceable() && autoRemineConfig.getValue())
                            {
                                stopMining(miningData);
                                if (!miningData.hasAttemptedBreak())
                                {
                                    miningData.setAttemptedBreak();
                                }
                            }
                            else if (!mc.world.isAir(cityBlockPos.pos()) && !isBlockDelayGrim())
                            {
                                MiningData data = new AutoMiningData(cityBlockPos.pos(),
                                        strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityBlockPos.pos()) : Direction.UP);
                                queueMiningData(data);
                            }
                        }
                    }
                }
            }
        }
        if (miningQueue.isEmpty())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (isDataPacketMine(data) && (data.getState().isAir() || data.hasAttemptedBreak() && data.passedAttemptedBreakTime(500)))
            {
                Managers.INVENTORY.syncToClient();
                miningQueue.remove(data);
                continue;
            }
            final float damageDelta = SpeedmineModule.getInstance().calcBlockBreakingDelta(
                    data.getState(), mc.world, data.getPos());
            data.damage(damageDelta);
            if (data.getBlockDamage() >= 0.95f && isDataPacketMine(data))
            {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue())
                {
                    return;
                }
                if (data.getSlot() != -1)
                {
                    Managers.INVENTORY.setSlot(data.getSlot());
                    if (!data.hasAttemptedBreak())
                    {
                        data.setAttemptedBreak();
                    }
                }
            }
        }
        MiningData miningData2 = miningQueue.getFirst();
        final double distance = mc.player.getEyePos().squaredDistanceTo(miningData2.getPos().toCenterPos());
        if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq())
        {
//          abortMining(miningData);
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
            miningData2.resetBreakTime();
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
            stopMining(miningData2);
            if (!miningData2.hasAttemptedBreak())
            {
                miningData2.setAttemptedBreak();
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
//              abortMining(miningData);
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
//              abortMining(miningData);
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
            int boxAlpha = (int) (80 * set.getValue().getFactor());
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
        MiningData miningData = new MiningData(pos, direction);
        queueMiningData(miningData);
    }

    private void queueMiningData(MiningData data)
    {
        if (miningQueue.stream().anyMatch(p1 -> data.getPos().equals(p1.getPos())))
        {
            return;
        }
        startMining(data);
        miningQueue.addFirst(data);
    }

    // LOL
    private PriorityQueue<AutoMineCalc> getNoAir(PriorityQueue<AutoMineCalc> calcs)
    {
        PriorityQueue<AutoMineCalc> noAir = new PriorityQueue<>();
        for (AutoMineCalc calc : calcs)
        {
            if (mc.world.isAir(calc.pos()))
            {
                continue;
            }
            noAir.add(calc);
        }
        return noAir;
    }

    private PriorityQueue<AutoMineCalc> getMiningPosition(PlayerEntity entity)
    {
        PriorityQueue<AutoMineCalc> miningPositions = new PriorityQueue<>();
        List<BlockPos> entityIntersections = PositionUtil.getAllInBox(entity.getBoundingBox(), entity.getBlockPos());
        for (BlockPos blockPos : entityIntersections)
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                continue;
            }
            if (!mc.world.getBlockState(blockPos).isReplaceable())
            {
                miningPositions.add(new AutoMineCalc(blockPos, Double.MAX_VALUE));
            }
        }
        List<BlockPos> surroundBlocks = SurroundModule.getInstance().getSurroundNoDown(entity);
        for (BlockPos blockPos : surroundBlocks)
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                continue;
            }
            // Check surrounding positions
            double damage = ExplosionUtil.getDamageTo(entity, blockPos.toCenterPos().subtract(0.0, -0.5, 0.0), AutoCrystalModule.getInstance().getIgnoreTerrain());
            miningPositions.add(new AutoMineCalc(blockPos, damage));
        }
        miningPositions.removeIf(c -> BlastResistantBlocks.isUnbreakable(c.pos()));
        return miningPositions;
    }

    private BlockPos getCrawlingMine()
    {
        BlockPos crawlingPos = mc.player.getBlockPos();
        switch (crawlingConfig.getValue())
        {
            case UP ->
            {
                if (!BlastResistantBlocks.isUnbreakable(crawlingPos.up()) && !mc.world.isAir(crawlingPos.up()))
                {
                    return crawlingPos.up();
                }
            }
            case DOWN ->
            {
                if (!BlastResistantBlocks.isUnbreakable(crawlingPos.down()) && !mc.world.isAir(crawlingPos.down()))
                {
                    return crawlingPos.down();
                }
            }
        }
        return null;
    }

    private record AutoMineCalc(BlockPos pos, double entityDamage) implements Comparable<AutoMineCalc>
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

    private void startMining(MiningData data)
    {
        if (data.getState().isAir() || data.isStarted())
        {
            return;
        }
        if (doubleBreakConfig.getValue() && !isMiningFloor(data))
        {
            if (grimConfig.getValue())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            }
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        }
        else
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        }
        data.setStarted();
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
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
        boolean canSwap = data.getSlot() != -1;
        if (canSwap)
        {
            Managers.INVENTORY.setSlot(data.getSlot());
        }
        stopMiningInternal(data);
        lastBreak = System.currentTimeMillis();
        if (canSwap)
        {
            Managers.INVENTORY.syncToClient();
        }
        if (rotateConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync(true);
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

    public static class AutoMiningData extends MiningData
    {

        public AutoMiningData(BlockPos pos, Direction direction)
        {
            super(pos, direction);
        }
    }

    public static class MiningData
    {

        private boolean attemptedBreak;
        private final Timer attemptBreakTimer = new CacheTimer();
        private final BlockPos pos;
        private final Direction direction;
        private float lastDamage;
        private float blockDamage;
        private boolean instantRemine;
        private boolean started;

        public MiningData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
        }

        public void setAttemptedBreak()
        {
            this.attemptedBreak = true;
            resetBreakTime();
        }

        public void resetBreakTime()
        {
            attemptBreakTimer.reset();
        }

        public boolean hasAttemptedBreak()
        {
            return attemptedBreak;
        }

        public boolean passedAttemptedBreakTime(long time)
        {
            return attemptBreakTimer.passed(time);
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

        public boolean isStarted()
        {
            return started;
        }

        public void setStarted()
        {
            this.started = true;
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }
    }

    private enum AntiCrawling
    {
        UP,
        DOWN,
        OFF
    }
}
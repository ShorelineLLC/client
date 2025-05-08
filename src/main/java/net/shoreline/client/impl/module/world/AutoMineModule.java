package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
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
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.CombatModule;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.SurroundModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class AutoMineModule extends CombatModule
{
    private static AutoMineModule INSTANCE;

    Config<Boolean> autoConfig = register(new BooleanConfig("Auto", "Automatically mines nearby players feet", false));
    Config<Selection> selectionConfig = register(new EnumConfig<>("Selection", "The selection of blocks mine", Selection.ALL, Selection.values(), () -> autoConfig.getValue()));
    Config<List<Block>> whitelistConfig = register(new BlockListConfig<>("Whitelist", "Valid block whitelist", Blocks.OBSIDIAN, Blocks.ENDER_CHEST));
    Config<List<Block>> blacklistConfig = register(new BlockListConfig<>("Blacklist", "Valid block blacklist", Blocks.SHULKER_BOX));
    Config<Boolean> avoidSelfConfig = register(new BooleanConfig("AvoidSelf", "Avoids mining blocks in your surround", false, () -> autoConfig.getValue()));
    Config<Boolean> strictDirectionConfig = register(new BooleanConfig("StrictDirection", "Only mines on visible faces", false, () -> autoConfig.getValue()));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> autoConfig.getValue()));
    Config<Boolean> antiCrawlConfig = register(new BooleanConfig("AntiCrawl", "Attempts to stop player from crawling", false));
    Config<Boolean> headConfig = register(new BooleanConfig("TargetBody", "Attempts to mine players face blocks", false, () -> autoConfig.getValue()));
    Config<Boolean> aboveHeadConfig = register(new BooleanConfig("TargetHead", "Attempts to mine above players head", false, () -> autoConfig.getValue()));
    Config<Boolean> doubleBreakConfig = register(new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false));
    Config<Integer> mineTicksConfig = register(new NumberConfig<>("MiningTicks", "The max number of ticks to hold a pickaxe for the packet mine", 5, 20, 60, () -> doubleBreakConfig.getValue()));
    Config<RemineMode> remineConfig = register(new EnumConfig<>("Remine", "Remines already mined blocks", RemineMode.NORMAL, RemineMode.values()));
    Config<Boolean> packetInstantConfig = register(new BooleanConfig("Fast", "Instant mines on packet", false, () -> remineConfig.getValue() == RemineMode.INSTANT));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 6.0f));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f));
    Config<Swap> swapConfig = register(new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values()));
    Config<Boolean> swapBeforeConfig = register(new BooleanConfig("SwapBefore", "Swaps before fully done mining", false, () -> swapConfig.getValue() != Swap.OFF));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates when mining the block", true));
    Config<Boolean> switchResetConfig = register(new BooleanConfig("SwitchReset", "Resets mining after switching items", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    Config<Boolean> grimNewConfig = register(new BooleanConfig("GrimV3", "Allows mining on new grim servers", false, () -> grimConfig.getValue()));
    Config<Color> colorConfig = register(new ColorConfig("MineColor", "The mine render color", Color.RED, false, false));
    Config<Color> colorDoneConfig = register(new ColorConfig("DoneColor", "The done render color", Color.GREEN, false, false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));
    Config<Boolean> smoothColorConfig = register(new BooleanConfig("SmoothColor", "Interpolates from start to done color", false, () -> false));

    private PlayerEntity playerTarget;
    private MineData packetMine, instantMine; // mining2 should always be the instant mine
    private boolean packetSwapBack;
    private boolean manualOverride;
    private final Timer remineTimer = new CacheTimer();

    private boolean changedInstantMine;
    private boolean waitForPacketMine;
    private boolean packetMineStuck;

    private boolean antiCrawlOverride;
    private int antiCrawlTicks;

    private final Queue<MineData> autoMineQueue = new ArrayDeque<>();
    private int autoMineTickDelay;

    private MineAnimation packetMineAnim = new MineAnimation(
            MineData.empty(), new Animation(true, 200));
    private MineAnimation instantMineAnim = new MineAnimation(
            MineData.empty(), new Animation(true, 200));

    public AutoMineModule()
    {
        super("AutoMine", "Automatically mines blocks", ModuleCategory.WORLD, 900);
        INSTANCE = this;
    }

    public static AutoMineModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        if (instantMine != null)
        {
            return String.format("%.1f", Math.min(instantMine.getBlockDamage(), 1.0f));
        }
        return super.getModuleData();
    }

    @Override
    public void onDisable()
    {
        autoMineQueue.clear();
        playerTarget = null;
        packetMine = null;
        if (instantMine != null)
        {
            abortMining(instantMine);
            instantMine = null;
        }
        packetMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));
        instantMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));
        autoMineTickDelay = 0;
        antiCrawlTicks = 0;
        manualOverride = false;
        antiCrawlOverride = false;
        waitForPacketMine = false;
        packetMineStuck = false;
        if (packetSwapBack)
        {
            Managers.INVENTORY.syncToClient();
            packetSwapBack = false;
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        PlayerEntity currentTarget = getClosestPlayer(enemyRangeConfig.getValue());
        boolean targetChanged = playerTarget != null && playerTarget != currentTarget;
        playerTarget = currentTarget;

        if (isInstantMineComplete())
        {
            if (changedInstantMine)
            {
                changedInstantMine = false;
            }
            if (waitForPacketMine)
            {
                waitForPacketMine = false;
            }
        }

        autoMineTickDelay--;
        antiCrawlTicks--;

        // Mining packet handling
        if (packetMine != null && packetMine.getTicksMining() > mineTicksConfig.getValue())
        {
            packetMineStuck = true;
            packetMineAnim.animation.setState(false);
            if (packetSwapBack)
            {
                Managers.INVENTORY.syncToClient();
                packetSwapBack = false;
            }
            packetMine = null;
            if (!isInstantMineComplete())
            {
                waitForPacketMine = true;
            }
        }

        if (packetMine != null)
        {
            final float damageDelta = SpeedmineModule.getInstance().calcBlockBreakingDelta(
                    packetMine.getState(), mc.world, packetMine.getPos());
            packetMine.addBlockDamage(damageDelta);

            int slot = packetMine.getBestSlot();
            float damageDone = packetMine.getBlockDamage() + (swapBeforeConfig.getValue()
                    || packetMineStuck ? damageDelta : 0.0f);
            if (damageDone >= 1.0f && slot != -1  && !checkMultitask())
            {
                Managers.INVENTORY.setSlot(slot);
                packetSwapBack = true;
                if (packetMineStuck)
                {
                    packetMineStuck = false;
                }
            }
        }

        if (packetSwapBack)
        {
            if (packetMine != null && canMine(packetMine.getState()))
            {
                packetMine.markAttemptedMine();
            }
            else
            {
                Managers.INVENTORY.syncToClient();
                packetSwapBack = false;
                packetMineAnim.animation.setState(false);
                packetMine = null;
                if (!isInstantMineComplete())
                {
                    waitForPacketMine = true;
                }
            }
        }

        if (instantMine != null)
        {
            final double distance = mc.player.getEyePos().squaredDistanceTo(instantMine.getPos().toCenterPos());
            if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq()
                    || instantMine.getTicksMining() > mineTicksConfig.getValue())
            {
                abortMining(instantMine);
                instantMineAnim.animation.setState(false);
                instantMine = null;
            }
        }

        if (instantMine != null)
        {
            final float damageDelta = SpeedmineModule.getInstance().calcBlockBreakingDelta(
                    instantMine.getState(), mc.world, instantMine.getPos());
            instantMine.addBlockDamage(damageDelta);

            if (instantMine.getBlockDamage() >= speedConfig.getValue())
            {
                boolean canMine = canMine(instantMine.getState());
                boolean canPlace = mc.world.canPlace(instantMine.getState(), instantMine.getPos(), ShapeContext.absent());
                if (canMine)
                {
                    instantMine.markAttemptedMine();
                }
                else
                {
                    instantMine.resetMiningTicks();
                    if (remineConfig.getValue() == RemineMode.NORMAL || remineConfig.getValue() == RemineMode.FAST)
                    {
                        instantMine.setTotalBlockDamage(0.0f, 0.0f);
                    }

                    if (manualOverride)
                    {
                        manualOverride = false;
                        // Clear our old manual mine
                        abortMining(instantMine);
                        instantMineAnim.animation.setState(false);
                        instantMine = null;
                    }
                }

                boolean passedRemine = remineConfig.getValue() == RemineMode.INSTANT || remineTimer.passed(500);
                if (instantMine != null && (remineConfig.getValue() == RemineMode.INSTANT
                        && packetInstantConfig.getValue() && packetMine == null && canPlace || canMine && passedRemine)
                        && (!checkMultitask() || multitaskConfig.getValue() || swapConfig.getValue() == Swap.OFF))
                {
                    stopMining(instantMine);
                    remineTimer.reset();

                    if (AutoCrystalModule.getInstance().isEnabled()
                            && AutoCrystalModule.getInstance().shouldPreForcePlace())
                    {
                        AutoCrystalModule.getInstance().placeCrystalForTarget(playerTarget, instantMine.getPos().down());
                    }

                    if (remineConfig.getValue() == RemineMode.FAST)
                    {
                        startMining(instantMine);
                    }
                }
            }
        }

        // Clear overrides
        if (manualOverride && (instantMine == null || instantMine.getGoal() != MiningGoal.MANUAL))
        {
            manualOverride = false;
        }

        if (antiCrawlOverride && (instantMine == null || instantMine.getGoal() != MiningGoal.PREVENT_CRAWL))
        {
            antiCrawlOverride = false;
        }

        if (autoConfig.getValue())
        {
            if (!autoMineQueue.isEmpty() && autoMineTickDelay <= 0)
            {
                MineData nextMine = autoMineQueue.poll();
                if (nextMine != null)
                {
                    startMining(nextMine);
                    autoMineTickDelay = 5;
                }
            }

            BlockPos antiCrawlPos = getAntiCrawlPos(playerTarget);
            if (antiCrawlOverride)
            {
                if (mc.player.getPose().equals(EntityPose.SWIMMING))
                {
                    antiCrawlTicks = 10;
                }

                if (antiCrawlTicks <= 0 || !isInstantMineComplete() && antiCrawlPos != null
                        && !instantMine.getPos().equals(antiCrawlPos))
                {
                    antiCrawlOverride = false;
                }
            }

            if (autoMineQueue.isEmpty() && !manualOverride && !antiCrawlOverride)
            {
                if (antiCrawlConfig.getValue() && mc.player.getPose().equals(EntityPose.SWIMMING) && antiCrawlPos != null)
                {
                    MineData data = new MineData(antiCrawlPos, strictDirectionConfig.getValue() ?
                            Managers.INTERACT.getInteractDirection(antiCrawlPos, false) : Direction.UP, MiningGoal.PREVENT_CRAWL);
                    if (isInstantMineComplete() || !instantMine.equals(data))
                    {
                        startAutoMine(data);
                        antiCrawlOverride = true;
                    }
                }

                else if (playerTarget != null && !targetChanged)
                {
                    BlockPos targetPos = EntityUtil.getRoundedBlockPos(playerTarget);
                    boolean bedrockPhased = PositionUtil.isBedrock(playerTarget.getBoundingBox(), targetPos) && !playerTarget.isCrawling();

                    if (!isInstantMineComplete() && checkDataY(instantMine, targetPos, bedrockPhased))
                    {
                        abortMining(instantMine);
                        instantMineAnim.animation.setState(false);
                        instantMine = null;
                    }

                    else if (packetMine != null && checkDataY(packetMine, targetPos, bedrockPhased))
                    {
                        packetMineAnim.animation.setState(false);
                        if (packetSwapBack)
                        {
                            Managers.INVENTORY.syncToClient();
                            packetSwapBack = false;
                        }
                        packetMine = null;
                        waitForPacketMine = false;
                    }

                    else
                    {
                        List<BlockPos> phasedBlocks = getPhaseBlocks(playerTarget, targetPos, bedrockPhased);

                        MineData bestMine;
                        if (!phasedBlocks.isEmpty())
                        {
                            BlockPos pos1 = phasedBlocks.removeFirst();
                            bestMine = new MineData(pos1, strictDirectionConfig.getValue() ?
                                    Managers.INTERACT.getInteractDirection(pos1, false) : Direction.UP);

                            if (packetMine == null && doubleBreakConfig.getValue() || isInstantMineComplete())
                            {
                                startAutoMine(bestMine);
                            }
                        }

                        else
                        {
                            List<BlockPos> miningBlocks = getMiningBlocks(playerTarget, targetPos, bedrockPhased);
                            bestMine = getInstantMine(miningBlocks, bedrockPhased);

                            if (bestMine != null && (packetMine == null && !changedInstantMine
                                    && doubleBreakConfig.getValue() || isInstantMineComplete()))
                            {
                                startAutoMine(bestMine);
                            }
                        }
                    }
                }

                else
                {
                    if (!isInstantMineComplete() && instantMine.getGoal() == MiningGoal.MINING_ENEMY)
                    {
                        abortMining(instantMine);
                        instantMineAnim.animation.setState(false);
                        instantMine = null;
                    }

                    if (packetMine != null && packetMine.getGoal() == MiningGoal.MINING_ENEMY)
                    {
                        packetMineAnim.animation.setState(false);
                        if (packetSwapBack)
                        {
                            Managers.INVENTORY.syncToClient();
                            packetSwapBack = false;
                        }
                        packetMine = null;
                        waitForPacketMine = false;
                    }
                }
            }
        }
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }

        event.cancel();

        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || !canMine(event.getState()) || isMining(event.getPos()))
        {
            return;
        }

        MineData data = new MineData(event.getPos(), event.getDirection(), MiningGoal.MANUAL);

        if (instantMine != null && instantMine.getGoal() == MiningGoal.MINING_ENEMY
                || packetMine != null && packetMine.getGoal() == MiningGoal.MINING_ENEMY)
        {
            manualOverride = true;
        }

        if (!doubleBreakConfig.getValue())
        {
            instantMine = data;
            startMining(instantMine);
            mc.player.swingHand(Hand.MAIN_HAND, false);
            return;
        }

        boolean updateChanged = false;
        if (!isInstantMineComplete() && !changedInstantMine)
        {
            if (packetMine == null)
            {
                packetMine = instantMine.copy();
                packetMineAnim = new MineAnimation(packetMine,
                        new Animation(true, fadeTimeConfig.getValue()));
            }
            else
            {
                updateChanged = true;
            }
        }

        instantMine = data;
        startMining(instantMine);
        mc.player.swingHand(Hand.MAIN_HAND, false);
        if (updateChanged)
        {
            changedInstantMine = true;
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue() && instantMine != null)
        {
            instantMine.setTotalBlockDamage(0.0f, 0.0f);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && canMine(packet.getState()))
        {
            if (antiCrawlOverride && packet.getPos().equals(getAntiCrawlPos(playerTarget)))
            {
                antiCrawlTicks = 10;
            }
        }
    }

    public void startAutoMine(MineData data)
    {
        if (!canMine(data.getState()) || isMining(data.getPos()))
        {
            return;
        }

        if (!doubleBreakConfig.getValue())
        {
            instantMine = data;
            autoMineQueue.offer(data);
            return;
        }

        if (changedInstantMine && !isInstantMineComplete() || waitForPacketMine)
        {
            return;
        }

        boolean updateChanged = false;
        if (!isInstantMineComplete() && !changedInstantMine)
        {
            if (packetMine == null)
            {
                packetMine = instantMine.copy();
                packetMineAnim = new MineAnimation(packetMine,
                        new Animation(true, fadeTimeConfig.getValue()));
            }
            else
            {
                updateChanged = true;
            }
        }

        instantMine = data;
        autoMineQueue.offer(data);

        if (updateChanged)
        {
            changedInstantMine = true;
        }
    }

    public MineData getInstantMine(List<BlockPos> miningBlocks, boolean bedrockPhased)
    {
        PriorityQueue<MineData> validInstantMines = new PriorityQueue<>();
        for (BlockPos blockPos : miningBlocks)
        {
            BlockState state1 = mc.world.getBlockState(blockPos);
            if (!isAutoMineBlock(state1.getBlock())) // bedrock mine exploit!!
            {
                continue;
            }

            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                continue;
            }

            BlockState state2 = mc.world.getBlockState(blockPos.down());
            if (bedrockPhased || state2.isOf(Blocks.OBSIDIAN) || state2.isOf(Blocks.BEDROCK))
            {
                Direction direction = strictDirectionConfig.getValue() ?
                        Managers.INTERACT.getInteractDirection(blockPos, false) : Direction.UP;

                validInstantMines.add(new MineData(blockPos, direction));
            }
        }

        if (validInstantMines.isEmpty())
        {
            return null;
        }

        return validInstantMines.peek();
    }

    public List<BlockPos> getPhaseBlocks(PlayerEntity player, BlockPos playerPos, boolean targetBedrockPhased)
    {
        List<BlockPos> phaseBlocks = PositionUtil.getAllInBox(player.getBoundingBox(),
                targetBedrockPhased && headConfig.getValue() ? playerPos.up() : playerPos);

        phaseBlocks.removeIf(p ->
        {
            BlockState state = mc.world.getBlockState(p);
            if (!isAutoMineBlock(state.getBlock()) || !canMine(state) || isMining(p))
            {
                return true;
            }

            double dist = mc.player.getEyePos().squaredDistanceTo(p.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                return true;
            }

            return avoidSelfConfig.getValue() && intersectsPlayer(p);
        });

        if (targetBedrockPhased && aboveHeadConfig.getValue())
        {
            phaseBlocks.add(playerPos.up(2));
        }

        return phaseBlocks;
    }

    /**
     *
     * @param player
     * @return A {@link Set} of potential blocks to mine for an enemy player
     */
    public List<BlockPos> getMiningBlocks(PlayerEntity player, BlockPos playerPos, boolean bedrockPhased)
    {
        List<BlockPos> surroundingBlocks = SurroundModule.getInstance().getSurroundNoDown(player, rangeConfig.getValue());
        List<BlockPos> miningBlocks;
        if (bedrockPhased)
        {
            List<BlockPos> facePlaceBlocks = new ArrayList<>();
            if (headConfig.getValue())
            {
                facePlaceBlocks.addAll(surroundingBlocks.stream().map(BlockPos::up).toList());
            }

            BlockState belowFeet = mc.world.getBlockState(playerPos.down());
            if (canMine(belowFeet))
            {
                facePlaceBlocks.add(playerPos.down());
            }
            miningBlocks = facePlaceBlocks;
        }
        else
        {
            miningBlocks = surroundingBlocks;
        }

        miningBlocks.removeIf(p -> avoidSelfConfig.getValue() && intersectsPlayer(p));
        return miningBlocks;
    }

    private BlockPos getAntiCrawlPos(PlayerEntity playerTarget)
    {
        if (!mc.player.isOnGround())
        {
            return null;
        }
        BlockPos crawlingPos = EntityUtil.getRoundedBlockPos(mc.player);
        boolean playerBelow = playerTarget != null && EntityUtil.getRoundedBlockPos(playerTarget).getY() < crawlingPos.getY();
        // We want to be same level as our opponent
        if (playerBelow)
        {
            BlockState state = mc.world.getBlockState(crawlingPos.down());
            if (isAutoMineBlock(state.getBlock()) && canMine(state))
            {
                return crawlingPos.down();
            }
        }
        else
        {
            BlockState state = mc.world.getBlockState(crawlingPos.up());
            if (isAutoMineBlock(state.getBlock()) && canMine(state))
            {
                return crawlingPos.up();
            }
        }
        return null;
    }

    private boolean checkDataY(MineData data, BlockPos targetPos, boolean bedrockPhased)
    {
        return data.getGoal() == MiningGoal.MINING_ENEMY && !bedrockPhased && data.getPos().getY() != targetPos.getY();
    }

    private boolean intersectsPlayer(BlockPos pos)
    {
        List<BlockPos> playerBlocks = SurroundModule.getInstance().getPlayerBlocks(mc.player);
        List<BlockPos> surroundingBlocks = SurroundModule.getInstance().getSurroundNoDown(mc.player);
        return playerBlocks.contains(pos) || surroundingBlocks.contains(pos);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }

        RenderBuffers.preRender();
        if (instantMineAnim != null && instantMineAnim.animation().getFactor() > 0.01f)
        {
            renderMiningData(event.getMatrices(), event.getTickDelta(),
                    instantMineAnim, true);
        }

        if (doubleBreakConfig.getValue() && packetMineAnim != null && packetMineAnim.animation().getFactor() > 0.01f)
        {
            renderMiningData(event.getMatrices(), event.getTickDelta(),
                    packetMineAnim, false);
        }
        RenderBuffers.postRender();
    }

    public void renderMiningData(MatrixStack matrixStack, float tickDelta,
                                 MineAnimation mineAnimation, boolean instantMine)
    {
        MineData data = mineAnimation.data();
        Animation animation = mineAnimation.animation();
        int boxAlpha = (int) (40 * animation.getFactor());
        int lineAlpha = (int) (100 * animation.getFactor());

        int boxColor;
        int lineColor;
        if (smoothColorConfig.getValue())
        {
            boxColor = !canMine(data.getState()) ? ((ColorConfig) colorDoneConfig).getRgb(boxAlpha) :
                    ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), ((ColorConfig) colorDoneConfig).getValue(boxAlpha), ((ColorConfig) colorConfig).getValue(boxAlpha)).getRGB();
            lineColor = !canMine(data.getState()) ? ((ColorConfig) colorDoneConfig).getRgb(lineAlpha) :
                    ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), ((ColorConfig) colorDoneConfig).getValue(lineAlpha), ((ColorConfig) colorConfig).getValue(lineAlpha)).getRGB();
        }
        else
        {
            boxColor = data.getBlockDamage() >= 0.95f || !canMine(data.getState()) ? ((ColorConfig) colorDoneConfig).getRgb(boxAlpha) : ((ColorConfig) colorConfig).getRgb(boxAlpha);
            lineColor = data.getBlockDamage() >= 0.95f || !canMine(data.getState()) ? ((ColorConfig) colorDoneConfig).getRgb(lineAlpha) : ((ColorConfig) colorConfig).getRgb(lineAlpha);
        }

        BlockPos mining = data.getPos();
        VoxelShape outlineShape = VoxelShapes.fullCube();
        if (!instantMine || data.getBlockDamage() < speedConfig.getValue())
        {
            outlineShape = data.getState().getOutlineShape(mc.world, mining);
            outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
        }
        Box render1 = outlineShape.getBoundingBox();
        Vec3d center = render1.offset(mining).getCenter();
        float total = instantMine ? speedConfig.getValue() : 1.0f;
        float scale = (instantMine && data.getBlockDamage() >= speedConfig.getValue()) || !canMine(data.getState()) ? 1.0f :
                MathHelper.clamp((data.getBlockDamage() + (data.getBlockDamage() - data.getLastDamage()) * tickDelta) / total, 0.0f, 1.0f);
        double dx = (render1.maxX - render1.minX) / 2.0;
        double dy = (render1.maxY - render1.minY) / 2.0;
        double dz = (render1.maxZ - render1.minZ) / 2.0;
        final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
        RenderManager.renderBox(matrixStack, scaled, boxColor);
        RenderManager.renderBoundingBox(matrixStack, scaled, 1.5f, lineColor);
    }

    public void startMining(MineData data)
    {
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

        if (doubleBreakConfig.getValue())
        {
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
            // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
            if (grimNewConfig.getValue())
            {
                if (!AnticheatModule.getInstance().getMiningFix())
                {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                }
                else
                {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                }

                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            else
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
        else
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        }

        if (rotateConfig.getValue() && grimConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync();
        }

        instantMineAnim = new MineAnimation(data, new Animation(true, fadeTimeConfig.getValue()));
    }

    public void abortMining(MineData data)
    {
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    public void stopMining(MineData data)
    {
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

        int slot = data.getBestSlot();
        if (slot != -1)
        {
            swapTo(slot);
        }

        stopMiningInternal(data);

        if (slot != -1)
        {
            swapSync(slot);
        }

        if (rotateConfig.getValue() && grimConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
    }

    private void stopMiningInternal(MineData data)
    {
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
    }

    public boolean isInstantMineComplete()
    {
        return instantMine == null || instantMine.getBlockDamage() >= speedConfig.getValue() && !canMine(instantMine.getState());
    }

    public BlockPos getMiningBlock()
    {
        if (instantMine != null)
        {
            double damage = instantMine.getBlockDamage() / speedConfig.getValue();
            if (damage > 0.75)
            {
                return instantMine.getPos();
            }
        }
        return null;
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

    public boolean isSilentSwapping()
    {
        return packetSwapBack;
    }

    private boolean isMining(BlockPos blockPos)
    {
        return instantMine != null && instantMine.getPos().equals(blockPos) ||
                packetMine != null && packetMine.getPos().equals(blockPos);
    }

    private boolean isAutoMineBlock(Block block)
    {
        if (BlastResistantBlocks.isUnbreakable(block))
        {
            return false;
        }
        return switch (selectionConfig.getValue())
        {
            case WHITELIST -> ((BlockListConfig<?>) whitelistConfig).contains(block);
            case BLACKLIST -> !((BlockListConfig<?>) blacklistConfig).contains(block);
            case ALL -> true;
        };
    }

    public boolean canMine(BlockState state)
    {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    public static class MineData implements Comparable<MineData>
    {
        private final BlockPos pos;
        private final Direction direction;
        private final MiningGoal goal;
        //
        private int ticksMining;
        private float blockDamage, lastDamage;

        public MineData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
            this.goal = MiningGoal.MINING_ENEMY;
        }

        public MineData(BlockPos pos, Direction direction, MiningGoal goal)
        {
            this.pos = pos;
            this.direction = direction;
            this.goal = goal;
        }

        private double getPriority()
        {
            double dist = mc.player.getEyePos().squaredDistanceTo(pos.down().toCenterPos());
            if (dist <= AutoCrystalModule.getInstance().getPlaceRange())
            {
                return 10.0f;
            }

            return 0.0f;
        }

        @Override
        public int compareTo(@NotNull MineData o)
        {
            return Double.compare(getPriority(), o.getPriority());
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof MineData d && d.getPos().equals(pos);
        }

        public void resetMiningTicks()
        {
            ticksMining = 0;
        }

        public void markAttemptedMine()
        {
            ticksMining++;
        }

        public void addBlockDamage(float blockDamage)
        {
            this.lastDamage = this.blockDamage;
            this.blockDamage += blockDamage;
        }

        public void setTotalBlockDamage(float blockDamage, float lastDamage)
        {
            this.blockDamage = blockDamage;
            this.lastDamage = lastDamage;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public MiningGoal getGoal()
        {
            return goal;
        }

        public int getTicksMining()
        {
            return ticksMining;
        }

        public float getBlockDamage()
        {
            return blockDamage;
        }

        public float getLastDamage()
        {
            return lastDamage;
        }

        public static MineData empty()
        {
            return new MineData(BlockPos.ORIGIN, Direction.UP);
        }

        public MineData copy()
        {
            final MineData data = new MineData(pos, direction, goal);
            data.setTotalBlockDamage(blockDamage, lastDamage);
            return data;
        }

        public BlockState getState()
        {
            return mc.world.getBlockState(pos);
        }

        public int getBestSlot()
        {
            return AutoToolModule.getInstance().getBestToolNoFallback(getState());
        }
    }

    public record MineAnimation(MineData data, Animation animation) {}

    public enum MiningGoal
    {
        MANUAL,
        MINING_ENEMY,
        PREVENT_CRAWL
    }

    public enum RemineMode
    {
        INSTANT,
        NORMAL,
        FAST
    }

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }
}

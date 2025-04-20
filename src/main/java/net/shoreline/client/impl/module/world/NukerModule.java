package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
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
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NukerModule extends CombatModule
{
    Config<Selection> selectionConfig = register(new EnumConfig<>("Selection", "The selection of blocks to use for scaffold", Selection.ALL, Selection.values()));
    Config<List<Block>> whitelistConfig = register(new BlockListConfig<>("Whitelist", "Valid block whitelist", Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN));
    Config<List<Block>> blacklistConfig = register(new BlockListConfig<>("Blacklist", "Valid block blacklist"));
    Config<NukeMode> modeConfig = register(new EnumConfig<>("Mode", "The nuker selection mode", NukeMode.SPHERE, NukeMode.values()));
    Config<Boolean> flattenConfig = register(new BooleanConfig("Flatten", "Only clears above the player y-level", false, () -> modeConfig.getValue() == NukeMode.SPHERE));
    Config<Boolean> strictDirectionConfig = register(new BooleanConfig("StrictDirection", "Only mines on visible faces", false));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mine blocks", 0.0f, 4.0f, 6.0f));
    Config<Boolean> doubleBreakConfig = register(new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false));
    Config<Integer> mineTicksConfig = register(new NumberConfig<>("MiningTicks", "The max number of ticks to hold a pickaxe for the packet mine", 5, 20, 60, () -> doubleBreakConfig.getValue()));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f));
    Config<Swap> swapConfig = register(new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values()));
    Config<Boolean> swapBeforeConfig = register(new BooleanConfig("SwapBefore", "Swaps before mining", false, () -> swapConfig.getValue() != Swap.OFF));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates when mining the block", true));
    Config<Boolean> switchResetConfig = register(new BooleanConfig("SwitchReset", "Resets mining after switching items", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    Config<Boolean> grimNewConfig = register(new BooleanConfig("GrimV3", "Allows mining on new grim servers", false, () -> grimConfig.getValue()));
    Config<Color> colorConfig = register(new ColorConfig("MineColor", "The mine render color", Color.RED, false, false));
    Config<Color> colorDoneConfig = register(new ColorConfig("DoneColor", "The done render color", Color.GREEN, false, false));
    Config<Boolean> debugTicksConfig = register(new BooleanConfig("Debug-Ticks", "Shows the mining ticks", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));
    Config<Boolean> smoothColorConfig = register(new BooleanConfig("SmoothColor", "Interpolates from start to done color", false, () -> false));

    private MineData packetMine, instantMine; // mining2 should always be the instant mine
    private boolean packetSwapBack;
    private boolean changedInstantMine;
    private boolean waitForPacketMine;

    private final Queue<BlockPos> selectedBlocks = new LinkedList<>();
    private final Queue<MineData> autoMineQueue = new ArrayDeque<>();
    private int autoMineTickDelay;

    private MineAnimation packetMineAnim = new MineAnimation(
            MineData.empty(), new Animation(true, 200));
    private MineAnimation instantMineAnim = new MineAnimation(
            MineData.empty(), new Animation(true, 200));

    public NukerModule()
    {
        super("Nuker", "Clears nearby blocks", ModuleCategory.WORLD);
    }

    @Override
    public void onDisable()
    {
        autoMineQueue.clear();
        packetMine = null;
        if (instantMine != null)
        {
            abortMining(instantMine);
            instantMine = null;
        }
        packetMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));
        instantMineAnim = new MineAnimation(MineData.empty(), new Animation(true, 200));
        autoMineTickDelay = 0;
        waitForPacketMine = false;
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

        // Mining packet handling
        if (packetMine != null && packetMine.getTicksMining() > mineTicksConfig.getValue())
        {
            packetMineAnim.animation.setState(false);
            if (packetSwapBack)
            {
                Managers.INVENTORY.syncToClient();
                packetSwapBack = false;
            }
            selectedBlocks.remove(packetMine.getPos());
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
            float damageDone = packetMine.getBlockDamage() + (swapBeforeConfig.getValue() ? damageDelta : 0.0f);
            if (damageDone >= 1.0f && canMine(packetMine.getState()) && slot != -1 && !checkMultitask())
            {
                packetMine.markAttemptedMine();
                Managers.INVENTORY.setSlot(slot);
                packetSwapBack = true;
            }
        }

        if (packetSwapBack && (packetMine == null || !canMine(packetMine.getState())))
        {
            Managers.INVENTORY.syncToClient();
            packetSwapBack = false;
            packetMineAnim.animation.setState(false);
            if (packetMine != null)
            {
                selectedBlocks.remove(packetMine.getPos());
            }
            packetMine = null;
            if (!isInstantMineComplete())
            {
                waitForPacketMine = true;
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
                selectedBlocks.remove(instantMine.getPos());
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
                if (canMine(instantMine.getState()))
                {
                    if (!checkMultitask() || multitaskConfig.getValue() || swapConfig.getValue() == Swap.OFF)
                    {
                        stopMining(instantMine);
                        instantMine.markAttemptedMine();
                    }
                }
                else
                {
                    abortMining(instantMine);
                    instantMineAnim.animation.setState(false);
                    selectedBlocks.remove(instantMine.getPos());
                    instantMine = null;
                }
            }
        }

        if (!autoMineQueue.isEmpty() && autoMineTickDelay <= 0)
        {
            MineData nextMine = autoMineQueue.poll();
            if (nextMine != null)
            {
                startMining(nextMine);
                autoMineTickDelay = 5;
            }
        }

        if (autoMineQueue.isEmpty())
        {
            MineData bestMine = getNukerMine();
            if (bestMine != null && (packetMine == null
                    && doubleBreakConfig.getValue() || isInstantMineComplete()))
            {
                startAutoMine(bestMine);
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
        if (event.getState().getBlock().getHardness() == -1.0f || !canMine(event.getState())
                || selectedBlocks.contains(event.getPos()))
        {
            return;
        }

        final double distance = mc.player.getEyePos().squaredDistanceTo(event.getPos().toCenterPos());
        if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq())
        {
            return;
        }

        selectedBlocks.add(event.getPos());
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue())
        {
            instantMine.setTotalBlockDamage(0.0f, 0.0f);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && !canMine(packet.getState())
                && packetMine != null && packetMine.getPos().equals(packet.getPos()))
        {
            packetMineAnim.animation.setState(false);
            if (packetSwapBack)
            {
                Managers.INVENTORY.syncToClient();
                packetSwapBack = false;
            }
            packetMine = null;
            waitForPacketMine = false;
            if (!isInstantMineComplete())
            {
                waitForPacketMine = true;
            }
        }
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(rangeConfig.getValue());
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = flattenConfig.getValue() ? 0.0 : -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
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

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator())
        {
            return;
        }

        RenderBuffers.preRender();

        for (BlockPos pos : selectedBlocks)
        {
            RenderManager.renderBoundingBox(event.getMatrices(), pos, 2.0f, ColorsModule.getInstance().getRGB(160));
        }

        if (instantMineAnim != null && instantMineAnim.animation().getFactor() > 0.01f)
        {
            renderMiningData(event.getMatrices(), event.getTickDelta(),
                    instantMineAnim, true);
        }

        if (packetMineAnim != null && packetMineAnim.animation().getFactor() > 0.01f)
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
        if (debugTicksConfig.getValue())
        {
            RenderManager.renderSign(String.valueOf(data.getTicksMining()), center, -1);
        }
    }

    // Should be sorted by y level and distance
    public MineData getNukerMine()
    {
        if (modeConfig.getValue() == NukeMode.SPHERE)
        {
            List<BlockPos> sphere = getSphere(mc.player.getPos());

            BlockPos minePos = null;
            int yLevel = -128;
            double dist = Double.MAX_VALUE;
            for (BlockPos blockPos : sphere)
            {
                BlockState state = mc.world.getBlockState(blockPos);
                if (!canMine(state) || isMining(blockPos))
                {
                    continue;
                }

                final double distance = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
                if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq())
                {
                    continue;
                }

                int y = blockPos.getY();
                double distToPlayer = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());

                if (y > yLevel || (y == yLevel && distToPlayer < dist))
                {
                    minePos = blockPos;
                    yLevel = y;
                    dist = distToPlayer;
                }
            }

            if (minePos != null)
            {
                return new MineData(minePos, strictDirectionConfig.getValue() ?
                        Managers.INTERACT.getInteractDirection(minePos, false) : Direction.UP);
            }
        }

        else
        {
            if (selectedBlocks.isEmpty())
            {
                return null;
            }

            if (packetMine != null && instantMine != null)
            {
                return null;
            }

            Queue<BlockPos> blocks = new LinkedList<>(selectedBlocks);
            BlockPos minePos = blocks.poll();
            if (instantMine != null && instantMine.getPos().equals(minePos))
            {
                minePos = selectedBlocks.poll();
            }
            if (minePos != null)
            {
                return new MineData(minePos, strictDirectionConfig.getValue() ?
                        Managers.INTERACT.getInteractDirection(minePos, false) : Direction.UP);
            }
        }

        return null;
    }

    public void startMining(MineData data)
    {
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
            if (!grimConfig.getValue())
            {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
            }
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
        boolean canSwap = slot != -1 && slot != Managers.INVENTORY.getServerSlot();
        if (canSwap)
        {
            swapTo(slot);
        }

        stopMiningInternal(data);

        if (canSwap)
        {
            swapSync(slot);
        }

        if (rotateConfig.getValue())
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

    private boolean isMining(BlockPos blockPos)
    {
        return instantMine != null && instantMine.getPos().equals(blockPos) ||
                packetMine != null && packetMine.getPos().equals(blockPos);
    }

    private boolean validNukerBlock(Block block)
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

    public static class MineData
    {
        private final BlockPos pos;
        private final Direction direction;
        //
        private int ticksMining;
        private float blockDamage, lastDamage;

        public MineData(BlockPos pos, Direction direction)
        {
            this.pos = pos;
            this.direction = direction;
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

        public static MineData empty()
        {
            return new MineData(BlockPos.ORIGIN, Direction.UP);
        }

        public MineData copy()
        {
            final MineData data = new MineData(pos, direction);
            data.setTotalBlockDamage(blockDamage, lastDamage);
            return data;
        }

        public BlockPos getPos()
        {
            return pos;
        }

        public Direction getDirection()
        {
            return direction;
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

    public enum Selection
    {
        WHITELIST,
        BLACKLIST,
        ALL
    }

    public enum NukeMode
    {
        SPHERE,
        SELECT
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }
}


package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.CombatModule;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorClientPlayerInteractionManager;
import net.shoreline.client.util.collection.FirstOutQueue;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.EnchantmentUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SpeedmineModule extends CombatModule
{
    private static SpeedmineModule INSTANCE;

    Config<SpeedmineMode> modeConfig = register(new EnumConfig<>("Mode", "The mining mode for speedmine", SpeedmineMode.PACKET, SpeedmineMode.values()));
    Config<Boolean> multitaskConfig = register(new BooleanConfig("Multitask", "Allows mining while using items", false, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Boolean> doubleBreakConfig = register(new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 6.0f, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f));
    Config<Boolean> instantConfig = register(new BooleanConfig("Instant", "Instantly mines already broken blocks", false));
    Config<Swap> swapConfig = register(new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values(), () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates when mining the block", true, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Boolean> switchResetConfig = register(new BooleanConfig("SwitchReset", "Resets mining after switching items", false, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    Config<Boolean> grimNewConfig = register(new BooleanConfig("GrimV3", "Uses new grim block breaking speeds", false, () -> grimConfig.getValue()));
    Config<Color> colorConfig = register(new ColorConfig("MineColor", "The mine render color", Color.RED, false, false, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Color> colorDoneConfig = register(new ColorConfig("DoneColor", "The done render color", Color.GREEN, false, false, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));
    Config<Boolean> smoothColorConfig = register(new BooleanConfig("ColorSmooth", "Interpolates from start to done color", false, () -> false));

    private final Map<MiningData, Animation> fadeList = new HashMap<>();
    private FirstOutQueue<MiningData> miningQueue = new FirstOutQueue<>(2);
    private long lastBreak;

    public SpeedmineModule()
    {
        super("Speedmine", "Mines blocks faster", ModuleCategory.WORLD, 900);
        INSTANCE = this;
    }

    public static SpeedmineModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        if (modeConfig.getValue() == SpeedmineMode.PACKET)
        {
            MiningData miningData = miningQueue.peek();
            if (miningData != null)
            {
                return String.format("%.1f", Math.min(miningData.getBlockDamage(), 1.0f));
            }
        }
        return super.getModuleData();
    }

    @Override
    protected void onDisable()
    {
        miningQueue.clear();
        fadeList.clear();
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
    public void onPlayerTick(final TickEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        if (modeConfig.getValue() == SpeedmineMode.DAMAGE)
        {
            AccessorClientPlayerInteractionManager interactionManager =
                    (AccessorClientPlayerInteractionManager) mc.interactionManager;
            if (interactionManager.hookGetCurrentBreakingProgress() >= speedConfig.getValue())
            {
                interactionManager.hookSetCurrentBreakingProgress(1.0f);
            }
            return;
        }

        if (AutoMineModule.getInstance().isEnabled())
        {
            return;
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
            if (isDataPacketMine(data) && (data.getState().isAir() || data.hasAttemptedBreak()
                    && data.passedAttemptedBreakTime(500)))
            {
                Managers.INVENTORY.syncToClient();
                miningQueue.remove(data);
                continue;
            }
            final float damageDelta = calcBlockBreakingDelta(data.getState(), mc.world, data.getPos());
            data.damage(damageDelta);
            if (isDataPacketMine(data) && data.getBlockDamage() >= 1.0f && data.getSlot() != -1)
            {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue())
                {
                    return;
                }

                if (data.getSlot() != Managers.INVENTORY.getServerSlot())
                {
                    Managers.INVENTORY.setSlot(data.getSlot());
                }
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
            return;
        }
        // Something went wrong, remove and remine
        if (miningData2.getBlockDamage() >= speedConfig.getValue() && miningData2.hasAttemptedBreak()
                && miningData2.passedAttemptedBreakTime(500))
        {
            abortMining(miningData2);
            miningQueue.remove(miningData2);
        }
        if (miningData2.getBlockDamage() >= speedConfig.getValue())
        {
            if (mc.player.isUsingItem() && !multitaskConfig.getValue())
            {
                return;
            }
            stopMining(miningData2);

            if (!instantConfig.getValue())
            {
                miningQueue.remove(miningData2);
            }

            if (!miningData2.hasAttemptedBreak())
            {
                miningData2.setAttemptedBreak(true);
            }
        }
    }

    @EventListener
    public void onAttackBlock(final AttackBlockEvent event)
    {
        if (mc.player.isCreative() || mc.player.isSpectator() || modeConfig.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (AutoMineModule.getInstance().isEnabled())
        {
            return;
        }
        event.cancel();

        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || event.getState().isAir())
        {
            return;
        }

        startManualMine(event.getPos(), event.getDirection());
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerActionC2SPacket packet
                && packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                && modeConfig.getValue() == SpeedmineMode.DAMAGE && grimConfig.getValue())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(500), packet.getDirection()));
        }

        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue()
                && modeConfig.getValue() == SpeedmineMode.PACKET)
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
        if (mc.player == null || modeConfig.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (AutoMineModule.getInstance().isEnabled())
        {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            handleBlockUpdatePacket(packet);
        }

        else if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof BlockUpdateS2CPacket packet2)
                {
                    handleBlockUpdatePacket(packet2);
                }
            }
        }
    }

    private void handleBlockUpdatePacket(BlockUpdateS2CPacket packet)
    {
        if (!packet.getState().isAir())
        {
            return;
        }
        for (MiningData data : miningQueue)
        {
            if (data.hasAttemptedBreak() && data.getPos().equals(packet.getPos()))
            {
                data.setAttemptedBreak(false);
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
        if (mc.player.isCreative() || modeConfig.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }

        if (AutoMineModule.getInstance().isEnabled())
        {
            return;
        }

        RenderBuffers.preRender();
        for (Map.Entry<MiningData, Animation> set : fadeList.entrySet())
        {
            MiningData data = set.getKey();
            set.getValue().setState(false);
            int boxAlpha = (int) (40 * set.getValue().getFactor());
            int lineAlpha = (int) (100 * set.getValue().getFactor());

            int boxColor;
            int lineColor;
            if (smoothColorConfig.getValue())
            {
                boxColor = data.getState().isAir() ? ((ColorConfig) colorDoneConfig).getRgb(boxAlpha) :
                        ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), ((ColorConfig) colorDoneConfig).getValue(boxAlpha), ((ColorConfig) colorConfig).getValue(boxAlpha)).getRGB();
                lineColor = data.getState().isAir() ? ((ColorConfig) colorDoneConfig).getRgb(lineAlpha) :
                        ColorUtil.interpolateColor(Math.min(data.getBlockDamage(), 1.0f), ((ColorConfig) colorDoneConfig).getValue(lineAlpha), ((ColorConfig) colorConfig).getValue(lineAlpha)).getRGB();
            }
            else
            {
                boxColor = data.getBlockDamage() >= 0.95f || data.getState().isAir() ? ((ColorConfig) colorDoneConfig).getRgb(boxAlpha) : ((ColorConfig) colorConfig).getRgb(boxAlpha);
                lineColor = data.getBlockDamage() >= 0.95f || data.getState().isAir() ? ((ColorConfig) colorDoneConfig).getRgb(lineAlpha) : ((ColorConfig) colorConfig).getRgb(lineAlpha);
            }

            BlockPos mining = data.getPos();
            VoxelShape outlineShape = data.getState().getOutlineShape(mc.world, mining);
            outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                    mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                    mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float total = isDataPacketMine(data) ? 1.0f : speedConfig.getValue();
            float scale = data.getState().isAir() ? 1.0f : MathHelper.clamp((data.getBlockDamage() + (data.getBlockDamage() - data.getLastDamage()) * event.getTickDelta()) / total, 0.0f, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
            RenderManager.renderBox(event.getMatrices(), scaled, boxColor);
            RenderManager.renderBoundingBox(event.getMatrices(), scaled, 1.5f, lineColor);
        }
        for (MiningData data : miningQueue)
        {
            if (data.getState().isAir())
            {
                continue;
            }
            Animation animation = new Animation(true, fadeTimeConfig.getValue());
            fadeList.put(data, animation);
        }
        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);
        RenderBuffers.postRender();
    }

    private void startManualMine(BlockPos pos, Direction direction)
    {
        clickMine(new MiningData(pos, direction));
    }

    public void clickMine(MiningData miningData)
    {
        int queueSize = miningQueue.size();
        if (queueSize <= 2)
        {
            queueMiningData(miningData);
        }
    }

    private void queueMiningData(MiningData data)
    {
        if (data.getState().isAir())
        {
            return;
        }
        if (startMining(data))
        {
            if (miningQueue.stream().anyMatch(p1 -> data.getPos().equals(p1.getPos())))
            {
                return;
            }
            miningQueue.addFirst(data);
        }
    }

    private boolean startMining(MiningData data)
    {
        if (data.isStarted())
        {
            return false;
        }

        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
        data.setStarted();
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
            return true;
        }

        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        return true;
    }

    private void abortMining(MiningData data)
    {
        if (!data.isStarted() || data.getState().isAir())
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
        boolean canSwap = slot != -1 && slot != Managers.INVENTORY.getServerSlot();
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
            Managers.ROTATION.setRotationSilentSync();
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
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection()));
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

    public float calcBlockBreakingDelta(BlockState state, BlockView world, BlockPos pos)
    {
        if (swapConfig.getValue() == Swap.OFF)
        {
            return state.calcBlockBreakingDelta(mc.player, mc.world, pos);
        }
        float f = state.getHardness(world, pos);
        if (f == -1.0f)
        {
            return 0.0f;
        }
        else
        {
            int i = canHarvest(state) ? 30 : 100;
            return getBlockBreakingSpeed(state) / f / (float) i;
        }
    }

    private float getBlockBreakingSpeed(BlockState block)
    {
        int tool = AutoToolModule.getInstance().getBestTool(block);
        float f = mc.player.getInventory().getStack(tool).getMiningSpeedMultiplier(block);
        if (f > 1.0F)
        {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            int i = EnchantmentUtil.getLevel(stack, Enchantments.EFFICIENCY);
            if (i > 0 && !stack.isEmpty())
            {
                f += (float) (i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player))
        {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
        {
            float g = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier())
            {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1e-4f;
            };
            f *= g;
        }
//        if (mc.player.isSubmergedIn(FluidTags.WATER) && EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.AQUA_AFFINITY) <= 0)
//        {
//            f /= 5.0f;
//        }
        if (!mc.player.isOnGround())
        {
            f /= 5.0f;
        }
        return f;
    }

    private boolean canHarvest(BlockState state)
    {
        if (state.isToolRequired())
        {
            int tool = AutoToolModule.getInstance().getBestTool(state);
            return mc.player.getInventory().getStack(tool).isSuitableFor(state);
        }
        return true;
    }

    public boolean isMining()
    {
        return !miningQueue.isEmpty();
    }

    public static class MiningData
    {
        private boolean attemptedBreak;
        private long breakTime;
        private final BlockPos pos;
        private final Direction direction;
        private float lastDamage;
        private float blockDamage;
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

    public enum SpeedmineMode
    {
        PACKET,
        DAMAGE
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
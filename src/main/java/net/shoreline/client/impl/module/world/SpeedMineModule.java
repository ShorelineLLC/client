package net.shoreline.client.impl.module.world;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.impl.Priorities;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.inventory.ItemSlot;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.mining.MiningPackets;
import net.shoreline.client.impl.mining.MiningUtil;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.util.entity.PlayerUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

@Getter
public class SpeedMineModule extends ListeningToggleable
{
    public static SpeedMineModule INSTANCE;

    Config<MiningPackets> miningPackets = new EnumConfig.Builder<MiningPackets>("Mode")
            .setValues(MiningPackets.values()).setDefaultValue(MiningPackets.NORMAL)
            .setDescription("The mode for block click packets").build();
    Config<Boolean> doubleMine = new BooleanConfig.Builder("DoubleMine")
            .setDescription("Rotates before mining block")
            .setDefaultValue(false).build();
    Config<RemineMode> remineMode = new EnumConfig.Builder<RemineMode>("Remine")
            .setValues(RemineMode.values())
            .setDescription("The mode for remining mined blocks")
            .setDefaultValue(RemineMode.OFF).build();
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The max range to mine").build();
    Config<Float> speedConfig = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.5f).setMax(1.0f).setDefaultValue(1.0f)
            .setDescription("The mining progress before breaking").build();
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while mining")
            .setDefaultValue(true).build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates before mining block")
            .setDefaultValue(false).build();
    Config<SilentSwapType> swapType = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The silent swap type")
            .setDefaultValue(SilentSwapType.HOTBAR).build();

    Config<BoxRender> boxMode = new EnumConfig.Builder<BoxRender>("BoxMode")
            .setValues(BoxRender.values())
            .setDescription("Box rendering mode")
            .setDefaultValue(BoxRender.FILL).build();
    Config<Color> miningColor = new ColorConfig.Builder("Mining")
            .setDescription("The color when mining a block")
            .setDefaultValue(Color.RED).build();
    Config<Color> breakingColor = new ColorConfig.Builder("Breaking")
            .setDescription("The color when breaking a block")
            .setDefaultValue(Color.GREEN).build();
    Config<Void> renderConfig = new ConfigGroup.Builder("Render")
            .addAll(boxMode, miningColor, breakingColor).build();

    @Setter
    private boolean isManualMining;

    private MiningData mainMiningBlock, packetMiningBlock;
    private MiningRenderState mainState, packetState;

    private MiningData pendingClear;

    public SpeedMineModule()
    {
        super("SpeedMine", new String[] {"SpeedyGonzales"}, "Mine faster", GuiCategory.WORLD);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return mainMiningBlock != null ? (mainMiningBlock.isDoneMining() ? Formatting.GREEN : Formatting.WHITE) +
                String.format("%.1f", Math.min(mainMiningBlock.getBlockDamage(), 1.0f)) : super.getModuleData();
    }

    @Override
    public void onDisable()
    {
        if (checkNull())
        {
            return;
        }

        clearMain();
        pendingClear = null;
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
        clearPacket();
        clearMain();
        pendingClear = null;
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            disable();
            clearPacket();
            clearMain();
            pendingClear = null;
        }
    }

    @EventListener(priority = Priorities.SPEED_MINE)
    public void onTickEvent(TickEvent.Pre event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (isEnabled())
        {
            tickMain();
        }

        tickPacket();
        if (isManualMining && mainMiningBlock == null)
        {
            isManualMining = false;
        }
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (checkNull() || !PlayerUtil.isInSurvival(mc.player))
        {
            return;
        }

        event.cancel();
        if (isMining(event.getPos()) || !MiningUtil.canMineBlock(event.getState()))
        {
            return;
        }

        isManualMining = true;
        startMining(event.getPos(), event.getDirection());
        mc.player.swingHand(Hand.MAIN_HAND, false);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (mainState != null)
        {
            if (mainState.getFactor() < 0.01f)
            {
                mainState = null;
                return;
            }

            mainState.data.render(event.getMatrixStack(),
                    event.getTickDelta(),
                    boxMode.getValue(),
                    miningColor.getValue().getRGB(),
                    breakingColor.getValue().getRGB(),
                    (float) Easing.SMOOTH_STEP.ease(mainState.getFactor()),
                    speedConfig.getValue());
        }

        if (packetState != null)
        {
            if (packetState.getFactor() < 0.01f)
            {
                packetState = null;
                return;
            }

            packetState.data.render(event.getMatrixStack(),
                    event.getTickDelta(),
                    boxMode.getValue(),
                    miningColor.getValue().getRGB(),
                    breakingColor.getValue().getRGB(),
                    (float) Easing.SMOOTH_STEP.ease(packetState.getFactor()), 1.0f);
        }
    }

    public void startMining(BlockPos blockPos, Direction direction)
    {
        float oldDamage = -1.0f;
        if (doubleMine.getValue())
        {
            if (pendingClear != null)
            {
                if (pendingClear.equals(mainMiningBlock) && mainMiningBlock.getBlockDamage() < speedConfig.getValue())
                {
                    clearMain();
                    if (pendingClear.getBlockPos().equals(blockPos))
                    {
                        oldDamage = pendingClear.getBlockDamage();
                    }
                }

                pendingClear = null;
            }

            if (mainMiningBlock != null && !mainMiningBlock.isBlockMined())
            {
                if (packetMiningBlock == null || packetMiningBlock.isBlockMined())
                {
                    packetMiningBlock = mainMiningBlock.copy(1.0f);
                    packetState = new MiningRenderState(packetMiningBlock, new Animation(true, 300L));
                }
            }
        }

        BlockState state = mc.world.getBlockState(blockPos);
        ItemSlot slot = AutoToolModule.INSTANCE.getBestTool(state);

        mainMiningBlock = MiningData.builder()
                .blockPos(blockPos)
                .direction(direction)
                .maxProgress(speedConfig.getValue())
                .player(mc.player)
                .miningStack(slot == null ? mc.player.getMainHandStack() : slot.getItemStack())
                .build();

        if (oldDamage > 0.0f)
        {
            mainMiningBlock.setBlockDamage(oldDamage);
        }

        mainState = new MiningRenderState(mainMiningBlock, new Animation(true, 300L));
        miningPackets.getValue().sendStartPackets(this,
                mainMiningBlock.getBlockPos(),
                mainMiningBlock.getDirection());

        mainMiningBlock.setStarted(true);
    }

    private void tickMain()
    {
        if (mainMiningBlock == null)
        {
            return;
        }

        if (mainMiningBlock.getSquaredDistanceTo() > MathHelper.square(rangeConfig.getValue()))
        {
            clearMain();
            return;
        }

        if (!mainMiningBlock.isStarted() && !mainMiningBlock.isAir() && remineMode.getValue() == RemineMode.FAST)
        {
            miningPackets.getValue().sendStartPackets(this,
                    mainMiningBlock.getBlockPos(),
                    mainMiningBlock.getDirection());

            mainMiningBlock.setStarted(true);
        }

        boolean multiTasking = mc.player.isUsingItem() && !multitaskConfig.getValue();
        float blockDamage = mainMiningBlock.tickDelta(multiTasking);
        if (blockDamage < speedConfig.getValue())
        {
            return;
        }

        if (mainMiningBlock.isAir())
        {
            mainMiningBlock.resetTicksMining();

            if (isManualMining)
            {
                isManualMining = false;
            }

            if (remineMode.getValue() != RemineMode.INSTANT)
            {
                mainMiningBlock.setBlockDamage(0.0f);
                mainMiningBlock.setLastDamage(0.0f);
                mainMiningBlock.setStarted(false);
                return;
            }

        } else if (mainMiningBlock.hasMinedFor(30))
        {
            clearMain();
            return;
        }

        if (multiTasking)
        {
            return;
        }

        ItemSlot bestTool = AutoToolModule.INSTANCE.getBestTool(mainMiningBlock.getBlockState());
        if (bestTool != null && !Managers.INVENTORY.startSwap(bestTool.getSlot(), swapType.getValue()))
        {
            return;
        }

        miningPackets.getValue().sendStopPackets(this,
                mainMiningBlock.getBlockPos(),
                mainMiningBlock.getDirection());

        Managers.INVENTORY.endSwap(swapType.getValue());

        if (remineMode.getValue() == RemineMode.OFF)
        {
            clearMain();
        }
    }

    private void tickPacket()
    {
        if (packetMiningBlock == null)
        {
            return;
        }

        boolean multiTasking = mc.player.isUsingItem() && !multitaskConfig.getValue();
        float blockDamage = packetMiningBlock.tickDelta(multiTasking);
        if (blockDamage < speedConfig.getValue())
        {
            return;
        }

        if (multiTasking)
        {
            return;
        }

        if (packetMiningBlock.isBlockMined() || packetMiningBlock.hasMinedFor(30))
        {
            if (mainMiningBlock != null && mainMiningBlock.getBlockDamage() < speedConfig.getValue())
            {
                pendingClear = mainMiningBlock;
            }

            clearPacket();
        } else
        {
            ItemSlot bestTool = AutoToolModule.INSTANCE.getBestTool(packetMiningBlock.getBlockState());
            if (bestTool == null)
            {
                return;
            }

            Managers.INVENTORY.startMultitickSwap(bestTool.getSlot());
        }
    }

    private void clearMain()
    {
        if (mainMiningBlock != null)
        {
            if (!mainMiningBlock.isDoneMining())
            {
                mainMiningBlock.abort(this);
            }

            mainMiningBlock = null;
        }

        if (mainState != null)
        {
            mainState.setState(false);
        }
    }

    private void clearPacket()
    {
        if (packetMiningBlock != null)
        {
            Managers.INVENTORY.endMultitickSwap();
            packetMiningBlock = null;
        }

        if (packetState != null)
        {
            packetState.setState(false);
        }
    }

    public boolean isMining(BlockPos blockPos)
    {
        return mainMiningBlock != null && mainMiningBlock.getBlockPos().equals(blockPos)
                || packetMiningBlock != null && packetMiningBlock.getBlockPos().equals(blockPos);
    }

    private record MiningRenderState(MiningData data, Animation animation)
    {
        public void setState(boolean state)
        {
            animation.setState(state);
        }

        public float getFactor()
        {
            return (float) animation.getFactor();
        }
    }

    private enum RemineMode
    {
        INSTANT, FAST, OFF
    }
}
package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.util.entity.PlayerUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class SpeedMineModule extends Toggleable
{
    Config<MiningMode> modeConfig = new EnumConfig.Builder<MiningMode>("Mode")
            .setValues(MiningMode.values()).setDefaultValue(MiningMode.NORMAL)
            .setDescription("The mode for block click packets").build();
    Config<Boolean> doubleMine = new BooleanConfig.Builder("DoubleMine")
            .setDescription("Rotates before mining block")
            .setVisible(() -> modeConfig.getValue() == MiningMode.GRIM)
            .setDefaultValue(false).build();
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The max range to mine").build();
    Config<Float> speedConfig = new NumberConfig.Builder<Float>("Progress")
            .setMin(0.1f).setMax(1.0f).setDefaultValue(1.0f)
            .setDescription("The mining progress before breaking").build();
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while mining")
            .setDefaultValue(true).build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates before mining block")
            .setDefaultValue(false).build();
    Config<SilentSwapType> swapConfig = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for swapping to pearls")
            .setDefaultValue(SilentSwapType.HOTBAR).build();

    Config<Color> miningColor = new ColorConfig.Builder("Mining")
            .setDescription("The color when mining a block")
            .setDefaultValue(Color.RED).build();
    Config<Color> breakingColor = new ColorConfig.Builder("Breaking")
            .setDescription("The color when breaking a block")
            .setDefaultValue(Color.GREEN).build();
    Config<Void> renderConfig = new ConfigGroup.Builder("Render")
            .addAll(miningColor, breakingColor).build();

    private MiningData mainMiningBlock, packetMiningBlock;

    public SpeedMineModule()
    {
        super("SpeedMine", new String[] {"SpeedyGonzales"}, "Mine faster", GuiCategory.WORLD);
    }

    @EventListener
    public void onTickEvent(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        if (mainMiningBlock != null)
        {
            float blockDamage = mainMiningBlock.tickBlockDamage();
            if (blockDamage >= speedConfig.getValue())
            {
                int slot = AutoToolModule.INSTANCE.getBestTool(mainMiningBlock.getBlockState());
                if (slot != -1 && Managers.INVENTORY.startSwap(slot, swapConfig.getValue()))
                {
                    stopMiningBlock(mainMiningBlock);
                    Managers.INVENTORY.endSwap();
                }
            }
        }
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (!PlayerUtil.isInSurvival(mc.player) || !canMineBlock(event.getState()))
        {
            return;
        }

        event.cancel();

        int slot = AutoToolModule.INSTANCE.getBestTool(event.getState());
        mainMiningBlock = MiningData.builder()
                .blockPos(event.getPos())
                .direction(event.getDirection())
                .player(mc.player)
                .miningStack(AutoToolModule.INSTANCE.getToolStack())
                .build();

        startMiningBlock(mainMiningBlock);
        mc.player.swingHand(Hand.MAIN_HAND, false);
    }

    private void startMiningBlock(MiningData data)
    {
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getBlockPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getBlockPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getBlockPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getBlockPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getBlockPos(), data.getDirection()));
        Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getBlockPos(), data.getDirection()));
    }

    private void stopMiningBlock(MiningData data)
    {
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getBlockPos(), data.getDirection(), id));
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (mainMiningBlock != null)
        {
            mainMiningBlock.render(event.getMatrixStack(), event.getTickDelta(), speedConfig.getValue(),
                    miningColor.getValue().getRGB(), breakingColor.getValue().getRGB());
        }
    }

    private boolean canMineBlock(BlockState state)
    {
        return state.getBlock().getHardness() != -1.0f && !state.isAir() && state.getFluidState().isEmpty();
    }

    private enum MiningMode
    {
        NORMAL,
        GRIM
    }
}

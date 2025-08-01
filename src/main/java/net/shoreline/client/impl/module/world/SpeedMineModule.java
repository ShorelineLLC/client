package net.shoreline.client.impl.module.world;

import net.minecraft.util.Hand;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.mining.MiningPackets;
import net.shoreline.client.impl.mining.MiningUtil;
import net.shoreline.client.util.entity.PlayerUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class SpeedMineModule extends Toggleable
{
    Config<MiningPackets> miningPackets = new EnumConfig.Builder<MiningPackets>("Mode")
            .setValues(MiningPackets.values()).setDefaultValue(MiningPackets.NORMAL)
            .setDescription("The mode for block click packets").build();
    Config<Boolean> doubleMine = new BooleanConfig.Builder("DoubleMine")
            .setDescription("Rotates before mining block")
            .setVisible(() -> miningPackets.getValue() == MiningPackets.GRIM)
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

    @Override
    public void onDisable()
    {
        mainMiningBlock = null;
        packetMiningBlock = null;
    }

    @EventListener
    public void onTickEvent(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        tickMain();
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (!PlayerUtil.isInSurvival(mc.player) || !MiningUtil.canMineBlock(event.getState()))
        {
            return;
        }

        event.cancel();

        int slot = AutoToolModule.INSTANCE.getBestTool(event.getState());
        mainMiningBlock = MiningData.builder()
                .blockPos(event.getPos())
                .direction(event.getDirection())
                .maxProgress(speedConfig.getValue())
                .player(mc.player)
                .miningStack(AutoToolModule.INSTANCE.getToolStack())
                .build();

        miningPackets.getValue().sendStartPackets(mainMiningBlock.getBlockPos(), mainMiningBlock.getDirection());
        mc.player.swingHand(Hand.MAIN_HAND, false);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (mainMiningBlock != null)
        {
            mainMiningBlock.render(event.getMatrixStack(), event.getTickDelta(),
                    miningColor.getValue().getRGB(), breakingColor.getValue().getRGB());
        }
    }

    private void tickMain()
    {
        if (mainMiningBlock == null)
        {
            return;
        }

        float blockDamage = mainMiningBlock.tickDelta();
        if (blockDamage < speedConfig.getValue())
        {
            return;
        }

        int slot = AutoToolModule.INSTANCE.getBestTool(mainMiningBlock.getBlockState());
        if (slot == -1 || !Managers.INVENTORY.startSwap(slot))
        {
            return;
        }

        miningPackets.getValue().sendStopPackets(mainMiningBlock.getBlockPos(), mainMiningBlock.getDirection());
        Managers.INVENTORY.endSwap();
    }
}

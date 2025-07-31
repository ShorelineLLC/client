package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class MineESPModule extends RenderModule
{
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(12.0f).setDefaultValue(6.0f).setFormat("m")
            .setDescription("The range to scan for mined blocks")
            .build();
    Config<Color> miningColor = new ColorConfig.Builder("Mining")
            .setDescription("The color when mining a block")
            .setDefaultValue(Color.YELLOW).build();
    Config<Color> breakingColor = new ColorConfig.Builder("Breaking")
            .setDescription("The color when breaking a block")
            .setDefaultValue(Color.ORANGE).build();

    public MineESPModule()
    {
        super("MineESP", "Highlights blocks being mined around you", GuiCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        for (MiningData data : Managers.MINING.getMiningBlocks())
        {
            float rangeSq = rangeConfig.getValue() * rangeConfig.getValue();
            if (mc.player.squaredDistanceTo(data.getBlockPos().toCenterPos()) > rangeSq || data.squaredDistanceTo() > rangeSq)
            {
                continue;
            }

            data.render(event.getMatrixStack(), event.getTickDelta(), 0.7f,
                    miningColor.getValue().getRGB(), breakingColor.getValue().getRGB());
        }
    }
}

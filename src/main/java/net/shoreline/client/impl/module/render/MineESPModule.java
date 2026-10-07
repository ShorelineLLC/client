package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MineESPModule extends RenderModule
{
    Config<BoxRender> boxMode = new EnumConfig.Builder<BoxRender>("Mode")
            .setValues(BoxRender.values())
            .setDescription("Box rendering mode")
            .setDefaultValue(BoxRender.FILL).build();
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(12.0f).setDefaultValue(6.0f).setFormat("m")
            .setDescription("The range to scan for mined blocks").build();
    Config<Color> miningColor = new ColorConfig.Builder("Mining")
            .setDescription("The color when mining a block")
            .setDefaultValue(Color.MAGENTA.darker()).build();
    Config<Color> breakingColor = new ColorConfig.Builder("Breaking")
            .setDescription("The color when breaking a block")
            .setDefaultValue(Color.MAGENTA.brighter()).build();

    private final ConcurrentMap<MiningData, Animation> fadeOutAnimations = new ConcurrentHashMap<>();

    public MineESPModule()
    {
        super("MineESP", "Highlights blocks being mined around you", GuiCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        for (Map.Entry<MiningData, Animation> animations : fadeOutAnimations.entrySet())
        {
            if (animations.getValue().getFactor() <= 0.01)
            {
                fadeOutAnimations.remove(animations.getKey());
                continue;
            }

            animations.getValue().setState(false);
            MiningData data = animations.getKey();

            data.render(event.getMatrixStack(), event.getTickDelta(),
                    boxMode.getValue(),
                    miningColor.getValue().getRGB(),
                    breakingColor.getValue().getRGB(),
                    (float) Easing.SMOOTH_STEP.ease(animations.getValue().getFactor()), 1.0f);
        }

        for (MiningData data : Managers.MINING.getMiningBlocks())
        {
            float rangeSq = rangeConfig.getValue() * rangeConfig.getValue();
            if (mc.player.squaredDistanceTo(data.getBlockPos().toCenterPos()) > rangeSq)
            {
                continue;
            }

            fadeOutAnimations.put(data, new Animation(true, 300L));
        }
    }
}

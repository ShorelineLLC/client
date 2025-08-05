package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.HoleData;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HoleESPModule extends RenderModule
{
    public static HoleESPModule INSTANCE;

    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(20.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to scan for holes").build();
    Config<Color> bedrockColor = new ColorConfig.Builder("BedrockColor")
            .setDescription("The color for bedrock holes")
            .setDefaultValue(Color.GREEN).build();
    Config<Color> obsidianColor = new ColorConfig.Builder("ObsidianColor")
            .setDescription("The color for bedrock obsidian")
            .setDefaultValue(Color.RED).build();
    Config<Color> mixedColor = new ColorConfig.Builder("MixedColor")
            .setDescription("The color for mixed holes")
            .setDefaultValue(Color.YELLOW).build();

    private List<HoleData> renderHoles = new ArrayList<>();

    public HoleESPModule()
    {
        super("HoleESP", "Highlights safe holes around you", GuiCategory.RENDER);
        INSTANCE = this;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        for (HoleData holeData : renderHoles)
        {
            BoxRender.FILL.render(event.getMatrixStack(), holeData.getBoundingBox(), getHoleColor(holeData));
        }

        List<HoleData> latestHoleData = Managers.HOLE.getResults();
        if (latestHoleData == null)
        {
            return;
        }

        renderHoles = latestHoleData;
    }

    private int getHoleColor(HoleData data)
    {
        return switch (data.getBlockType())
        {
            case OBSIDIAN -> obsidianColor.getValue().getRGB();
            case BEDROCK -> bedrockColor.getValue().getRGB();
            case MIXED -> mixedColor.getValue().getRGB();
        };
    }

    public float getRange()
    {
        return rangeConfig.getValue();
    }
}

package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.hole.HoleBlockType;
import net.shoreline.client.impl.combat.hole.HoleData;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HoleESPModule extends RenderModule
{
    public static HoleESPModule INSTANCE;

    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(20.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to scan for holes").build();
    Config<Boolean> showObsidian = new BooleanConfig.Builder("ShowObsidian")
            .setDescription("Renders obsidian holes")
            .setDefaultValue(true).build();
    Config<Boolean> showMixed = new BooleanConfig.Builder("ShowMixed")
            .setDescription("Renders mixed holes")
            .setDefaultValue(true).build();

    Config<Boolean> doublesConfig = new BooleanConfig.Builder("2x1")
            .setDescription("Scans for double holes")
            .setDefaultValue(false).build();
    Config<Boolean> quadsConfig = new BooleanConfig.Builder("2x2")
            .setDescription("Scans for quad holes")
            .setDefaultValue(false).build();
    Config<Void> typesConfig = new ConfigGroup.Builder("Types")
            .addAll(doublesConfig, quadsConfig).build();

    Config<BoxRender> modeConfig = new EnumConfig.Builder<BoxRender>("Mode")
            .setValues(BoxRender.values())
            .setDescription("Box rendering mode")
            .setDefaultValue(BoxRender.FILL).build();
    Config<Float> boxHeight = new NumberConfig.Builder<Float>("Height")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(1.0f)
            .setDescription("The box render height").build();
    Config<Color> bedrockColor = new ColorConfig.Builder("BedrockColor")
            .setDescription("The color for bedrock holes")
            .setDefaultValue(Color.GREEN).build();
    Config<Color> obsidianColor = new ColorConfig.Builder("ObsidianColor")
            .setDescription("The color for bedrock obsidian")
            .setVisible(() -> showObsidian.getValue())
            .setDefaultValue(Color.RED).build();
    Config<Color> mixedColor = new ColorConfig.Builder("MixedColor")
            .setDescription("The color for mixed holes")
            .setVisible(() -> showMixed.getValue())
            .setDefaultValue(Color.YELLOW).build();

    private final ConcurrentMap<HoleData, Animation> fadeAnimations = new ConcurrentHashMap<>();

    public HoleESPModule()
    {
        super("HoleESP", "Highlights safe holes around you", GuiCategory.RENDER);
        INSTANCE = this;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        Collection<HoleData> latestHoleData = Managers.HOLE.getResults();

        for (HoleData hole : latestHoleData)
        {
            if (hole.getBlockType() == HoleBlockType.OBSIDIAN && !showObsidian.getValue() ||
                    hole.getBlockType() == HoleBlockType.MIXED && !showMixed.getValue())
            {
                continue;
            }

            fadeAnimations.computeIfAbsent(hole, h -> new Animation(false, 300));
        }

        fadeAnimations.entrySet().removeIf(entry ->
        {
            HoleData holeData = entry.getKey();
            Animation anim = entry.getValue();

            double dist = holeData.squaredDistanceTo(mc.player);
            if (dist > rangeConfig.getValue() * rangeConfig.getValue()
                    || mc.player.getBoundingBox().intersects(holeData.getBoundingBox(0.5)))
            {
                anim.setState(false);
            }
            else
            {
                anim.setState(latestHoleData.contains(holeData));
            }

            if (holeData.getBlockType() == HoleBlockType.OBSIDIAN && !showObsidian.getValue()
                    || holeData.getBlockType() == HoleBlockType.MIXED && !showMixed.getValue())
            {
                return true;
            }

            return anim.getFactor() <= 0.01 && !latestHoleData.contains(holeData);
        });

        for (Map.Entry<HoleData, Animation> entry : fadeAnimations.entrySet())
        {
            HoleData holeData = entry.getKey();
            Animation anim = entry.getValue();

            modeConfig.getValue().render(event.getMatrixStack(),
                    holeData.getBoundingBox(boxHeight.getValue()),
                    getHoleColor(holeData),
                    (float) Easing.SMOOTH_STEP.ease(anim.getFactor()));
        }
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

    public boolean shouldGetDoubles()
    {
        return isEnabled() && doublesConfig.getValue();
    }

    public boolean shouldGetQuads()
    {
        return isEnabled() && quadsConfig.getValue();
    }

    public float getRange()
    {
        return rangeConfig.getValue();
    }
}

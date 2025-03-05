package net.shoreline.client.impl.module.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.manager.combat.hole.Hole;
import net.shoreline.client.impl.manager.combat.hole.HoleType;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author linus
 * @since 1.0
 */
public class HoleESPModule extends ToggleModule
{
    private static HoleESPModule INSTANCE;

    //
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "Range to display holes", 3.0f, 5.0f, 25.0f));
    Config<Boolean> outlineConfig = register(new BooleanConfig("Outline", "Renders an outline around the hole", true));
    Config<Boolean> crossConfig = register(new BooleanConfig("Cross", "Renders a cross on the floor of the hole", false));
    Config<Float> heightConfig = register(new NumberConfig<>("Size", "Render height of holes", -1.0f, 1.00f, 1.0f));
    Config<Boolean> ignoreSelfConfig = register(new BooleanConfig("IgnoreSelf", "Ignores the hole the player is standing in", false));
    Config<Boolean> obsidianCheckConfig = register(new BooleanConfig("Obsidian", "Displays obsidian holes", true));
    Config<Boolean> obsidianBedrockConfig = register(new BooleanConfig("Obsidian-Bedrock", "Displays mixed obsidian and bedrock holes", true));
    Config<Boolean> doubleConfig = register(new BooleanConfig("Double", "Displays double holes where the player can stand in the middle of two blocks to block explosion damage", false));
    Config<Boolean> quadConfig = register(new BooleanConfig("Quad", "Displays quad holes where the player can stand in the middle of four blocks to block explosion damage", false));
    Config<Boolean> voidConfig = register(new BooleanConfig("Void", "Displays void holes in the world", false));
    Config<Color> obsidianConfig = register(new ColorConfig("ObsidianColor", "The color for rendering obsidian holes", new Color(255, 0, 0, 80), () -> obsidianCheckConfig.getValue()));
    Config<Color> mixedConfig = register(new ColorConfig("Obsidian-BedrockColor", "The color for rendering mixed holes", new Color(255, 255, 0, 80), () -> obsidianBedrockConfig.getValue()));
    Config<Color> bedrockConfig = register(new ColorConfig("BedrockColor", "The color for rendering bedrock holes", new Color(0, 255, 0, 80)));
    Config<Color> voidColorConfig = register(new ColorConfig("VoidColor", "The color for rendering bedrock holes", new Color(255, 0, 0, 140), () -> voidConfig.getValue()));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 300, 1000, () -> false));

    private final Map<Hole, Animation> fadeList = new HashMap<>();

    public HoleESPModule()
    {
        super("HoleESP", "Displays nearby blast resistant holes", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onDisable()
    {
        fadeList.clear();
    }

    public static HoleESPModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.player == null)
        {
            return;
        }
        for (Hole hole : Managers.HOLE.getHoles())
        {
            if (!containsPos(fadeList.keySet(), hole))
            {
                Animation anim = new Animation(false, fadeTimeConfig.getValue());
                fadeList.put(hole, anim);
            }
        }

        RenderBuffers.preRender();
        for (Map.Entry<Hole, Animation> set : fadeList.entrySet())
        {
            Hole hole = set.getKey();
            double dist = hole.squaredDistanceTo(mc.player);

            if (dist > ((NumberConfig) rangeConfig).getValueSq())
            {
                set.getValue().setState(false);
            }
            else if (ignoreSelfConfig.getValue() && mc.player.getBoundingBox().intersects(hole.getBoundingBox(0.5)))
            {
                set.getValue().setState(false);
            }
            else if ((hole.isDoubleX() || hole.isDoubleZ()) && !doubleConfig.getValue()
                    || hole.isQuad() && !quadConfig.getValue()
                    || hole.getSafety() == HoleType.VOID && !voidConfig.getValue()
                    || hole.getSafety() == HoleType.OBSIDIAN && !obsidianCheckConfig.getValue()
                    || hole.getSafety() == HoleType.OBSIDIAN_BEDROCK && !obsidianBedrockConfig.getValue())
            {
                set.getValue().setState(false);
            }
            else
            {
                set.getValue().setState(containsPos(Managers.HOLE.getHoles(), hole));
            }

            if (set.getValue().getFactor() < 0.01f)
            {
                continue;
            }
            Color color = getHoleColor(hole.getSafety());
            int boxAlpha = (int) (color.getAlpha() * set.getValue().getFactor());
            int lineAlpha = (int) (100 * set.getValue().getFactor());

            renderHole(event.getMatrices(), hole, getHoleColor(hole.getSafety(), boxAlpha),
                    getHoleColor(hole.getSafety(), lineAlpha));
        }

        RenderBuffers.postRender();
    }

    private void renderHole(MatrixStack matrixStack, Hole hole, int color1, int color2)
    {
        Box render = hole.getBoundingBox(heightConfig.getValue());
        RenderManager.renderBox(matrixStack, render, color1);
        if (outlineConfig.getValue())
        {
            RenderManager.renderBoundingBox(matrixStack, render, 1.5f, color2);
        }
        if (crossConfig.getValue())
        {
            RenderManager.renderBoundingCross(matrixStack, render, 2.0f, color2);
        }
    }

    private boolean containsPos(Set<Hole> set, Hole hole)
    {
        return set.stream().anyMatch(hole1 ->
        {
            if (hole1.isDoubleX() != hole.isDoubleX() || hole1.isDoubleZ() != hole.isDoubleZ()
                    || hole1.isQuad() != hole.isQuad() || hole1.isStandard() != hole.isStandard())
            {
                return false;
            }
            if (hole.getSafety() != hole1.getSafety())
            {
                return false;
            }
            return hole.equals(hole1);
        });
    }

    private int getHoleColor(HoleType holeType, int alpha)
    {
        return switch (holeType)
        {
            case OBSIDIAN -> ((ColorConfig) obsidianConfig).getRgb(alpha);
            case OBSIDIAN_BEDROCK -> ((ColorConfig) mixedConfig).getRgb(alpha);
            case BEDROCK -> ((ColorConfig) bedrockConfig).getRgb(alpha);
            case VOID -> ((ColorConfig) voidColorConfig).getRgb(alpha);
        };
    }

    private Color getHoleColor(HoleType holeType)
    {
        return switch (holeType)
        {
            case OBSIDIAN -> obsidianConfig.getValue();
            case OBSIDIAN_BEDROCK -> mixedConfig.getValue();
            case BEDROCK -> bedrockConfig.getValue();
            case VOID -> voidColorConfig.getValue();
        };
    }

    public double getRange()
    {
        return rangeConfig.getValue();
    }
}

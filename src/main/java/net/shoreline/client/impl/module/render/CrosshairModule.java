package net.shoreline.client.impl.module.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.world.GameMode;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.gui.hud.RenderCrosshairEvent;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class CrosshairModule extends ToggleModule
{
    Config<Float> lengthConfig = register(new NumberConfig<>("Length", "The crosshair length", 0.0f, 1.0f, 2.5f));
    Config<Float> thicknessConfig = register(new NumberConfig<>("Thickness", "The crosshair thickness", 0.1f, 0.5f, 2.0f));
    Config<Integer> gapConfig = register(new NumberConfig<>("Gap", "The gap between the lines", 1, 2, 5));
    Config<Boolean> dynamicConfig = register(new BooleanConfig("Dynamic", "Indicates when the player is moving", false));
    Config<Boolean> outlineConfig = register(new BooleanConfig("Outline", "Outlines the crosshair", true));
    Config<Float> outlineThicknessConfig = register(new NumberConfig<>("OutlineThickness", "The width of the outline", 0.1f, 0.3f, 0.5f, () -> outlineConfig.getValue()));
    Config<Float> opacityConfig = register(new NumberConfig<>("Opacity", "The crosshair opacity", 0.10f, 1.00f, 1.00f));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The crosshair color", Color.WHITE));

    private final Animation gapAnimation = new Animation(false, 100L, Easing.LINEAR);

    public CrosshairModule()
    {
        super("Crosshair", "Custom crosshairs", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderCrosshair(RenderCrosshairEvent event)
    {
        event.cancel();

        if (!mc.options.getPerspective().isFirstPerson() || mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
        {
            return;
        }

        DrawContext context = event.getContext();
        float x = (context.getScaledWindowWidth() / 2.0f) - 0.5f;
        float y = (context.getScaledWindowHeight() / 2.0f) - 0.5f;
        float halfLength = (lengthConfig.getValue() * 10) / 2.0f;

        float o1 = outlineThicknessConfig.getValue();
        float o2 = o1 * 2.0f;
        float o3 = o1 * 3.0f;

        boolean moving = MovementUtil.isMovingInput() || mc.player.isSneaking() || mc.player.isClimbing() || !mc.player.isOnGround();
        double gap = gapConfig.getValue();
        if (dynamicConfig.getValue())
        {
            gapAnimation.setState(moving);
            gap += 2.0f * gapAnimation.getFactor();
        }

        if (outlineConfig.getValue())
        {
            RenderManager.rect(context.getMatrices(), x - halfLength - gap - o2, y - thicknessConfig.getValue() - o1,
                    halfLength + o3, thicknessConfig.getValue() * 2.0f + o2, ColorUtil.withAlpha(Color.BLACK.getRGB(), opacityConfig.getValue()));
            RenderManager.rect(context.getMatrices(), x + gap - o2, y - thicknessConfig.getValue() - o1,
                    halfLength + o3, thicknessConfig.getValue() * 2.0f + o2, ColorUtil.withAlpha(Color.BLACK.getRGB(), opacityConfig.getValue()));
        }

        RenderManager.rect(context.getMatrices(), x - halfLength - gap, y - thicknessConfig.getValue(),
                halfLength, thicknessConfig.getValue() * 2.0f, ColorUtil.withAlpha(colorConfig.getValue().getRGB(), opacityConfig.getValue()));
        RenderManager.rect(context.getMatrices(), x + gap, y - thicknessConfig.getValue(),
                halfLength, thicknessConfig.getValue() * 2.0f, ColorUtil.withAlpha(colorConfig.getValue().getRGB(), opacityConfig.getValue()));

        if (outlineConfig.getValue())
        {
            RenderManager.rect(context.getMatrices(), x - thicknessConfig.getValue() - o1, y - halfLength - gap - o1,
                    thicknessConfig.getValue() * 2.0f + o2, halfLength + o3, ColorUtil.withAlpha(Color.BLACK.getRGB(), opacityConfig.getValue()));
            RenderManager.rect(context.getMatrices(), x - thicknessConfig.getValue() - o1, y + gap - o1,
                    thicknessConfig.getValue() * 2.0f + o2, halfLength + o3, ColorUtil.withAlpha(Color.BLACK.getRGB(), opacityConfig.getValue()));
        }

        RenderManager.rect(context.getMatrices(), x - thicknessConfig.getValue(), y - halfLength - gap,
                thicknessConfig.getValue() * 2.0f, halfLength, ColorUtil.withAlpha(colorConfig.getValue().getRGB(), opacityConfig.getValue()));
        RenderManager.rect(context.getMatrices(), x - thicknessConfig.getValue(), y + gap,
                thicknessConfig.getValue() * 2.0f, halfLength, ColorUtil.withAlpha(colorConfig.getValue().getRGB(), opacityConfig.getValue()));
    }
}

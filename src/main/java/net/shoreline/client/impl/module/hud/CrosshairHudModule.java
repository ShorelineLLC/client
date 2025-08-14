package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.GameMode;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.module.impl.HudModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class CrosshairHudModule extends HudModule
{
    Config<Float> lengthConfig = new NumberConfig.Builder<Float>("Length")
            .setMin(0.0f).setMax(2.5f).setDefaultValue(1.0f)
            .setDescription("The crosshair length").build();
    Config<Float> thicknessConfig = new NumberConfig.Builder<Float>("Thickness")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(0.5f)
            .setDescription("The crosshair thickness").build();
    Config<Integer> gapConfig = new NumberConfig.Builder<Integer>("Gap")
            .setMin(1).setMax(5).setDefaultValue(2)
            .setDescription("The gap between the lines").build();
    Config<Boolean> dynamicConfig = new BooleanConfig.Builder("Dynamic")
            .setDescription("Indicates when the player is moving")
            .setDefaultValue(false).build();
    Config<Boolean> outlineConfig = new BooleanConfig.Builder("Outline")
            .setDescription("Outlines the crosshair")
            .setDefaultValue(true).build();
    Config<Float> outlineThicknessConfig = new NumberConfig.Builder<Float>("OutlineThickness")
            .setMin(0.1f).setMax(0.5f).setDefaultValue(0.3f)
            .setDescription("The width of the outline")
            .setVisible(() -> outlineConfig.getValue()).build();
    Config<Float> opacityConfig = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.10f).setMax(1.00f).setDefaultValue(1.00f)
            .setDescription("The crosshair opacity").build();
    Config<Color> colorConfig = new ColorConfig.Builder("Color")
            .setDescription("The crosshair color")
            .setDefaultValue(Color.WHITE).build();

    private final Animation gapAnim = new Animation(false, 100L);

    public CrosshairHudModule()
    {
        super("Crosshair", "Customize the game crosshair", 0, 0);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {

    }

    @Override
    public void drawGuiComponent(DrawContext context, float tickDelta)
    {
        drawCrosshair(context);
    }

    @EventListener
    public void onRenderCrosshair(HudOverlayEvent.Crosshair event)
    {
        event.cancel();
        drawCrosshair(event.getContext());
    }

    private void drawCrosshair(DrawContext context)
    {
        if (!mc.options.getPerspective().isFirstPerson() || mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
        {
            return;
        }

        float x = context.getScaledWindowWidth() / 2.0f;
        float y = context.getScaledWindowHeight() / 2.0f;
        float halfLength = (lengthConfig.getValue() * 10) / 2.0f;

        float o1 = outlineThicknessConfig.getValue();
        float o2 = o1 * 2.0f;
        float o3 = o1 * 3.0f;

        boolean moving = InputUtil.isInputtingMovement() || mc.player.isSneaking() || mc.player.isClimbing() || !mc.player.isOnGround();
        float gap = gapConfig.getValue();
        if (dynamicConfig.getValue())
        {
            gapAnim.setState(moving);
            gap += 2.5f * (float) Easing.SMOOTH_STEP.ease(gapAnim.getFactor());
        }

        float width = (halfLength + o3);
        float height = thicknessConfig.getValue() * 2.0f + o2;
        float x1 = x - halfLength - gap - o2;
        float y1 = y - thicknessConfig.getValue() - o1;
        if (outlineConfig.getValue())
        {
            context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(Color.BLACK.getRGB(), opacityConfig.getValue()));

            x1 = x + gap - o2;
            y1 = y - thicknessConfig.getValue() - o1;
            context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(Color.BLACK.getRGB(), opacityConfig.getValue()));
        }

        width = halfLength;
        height = thicknessConfig.getValue() * 2.0f;
        x1 = x - halfLength - gap;
        y1 = y - thicknessConfig.getValue();
        context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(colorConfig.getValue().getRGB(), opacityConfig.getValue()));

        x1 = x + gap;
        context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(colorConfig.getValue().getRGB(), opacityConfig.getValue()));

        if (outlineConfig.getValue())
        {
            width = thicknessConfig.getValue() * 2.0f + o2;
            height = halfLength + o3;
            x1 = x - thicknessConfig.getValue() - o1;
            y1 = y - halfLength - gap - o1;
            context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(Color.BLACK.getRGB(), opacityConfig.getValue()));

            x1 = x - thicknessConfig.getValue() - o1;
            y1 = y + gap - o1;
            context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(Color.BLACK.getRGB(), opacityConfig.getValue()));
        }

        width = thicknessConfig.getValue() * 2.0f;
        height = halfLength;
        x1 = x - thicknessConfig.getValue();
        y1 = y - halfLength - gap;
        context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(colorConfig.getValue().getRGB(), opacityConfig.getValue()));

        y1 = y + gap;
        context.fill((int) x1, (int) y1, (int) (x1 + width), (int) (y1 + height), ColorUtil.withTransparency(colorConfig.getValue().getRGB(), opacityConfig.getValue()));
    }

    @Override
    public int getWidth()
    {
        return 0;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }
}

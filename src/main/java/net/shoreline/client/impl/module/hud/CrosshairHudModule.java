package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.world.GameMode;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

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
            .setMin(0.1f).setMax(1.0f).setDefaultValue(0.5f)
            .setDescription("The width of the outline")
            .setVisible(() -> outlineConfig.getValue()).build();
    Config<Float> opacityConfig = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.10f).setMax(1.00f).setDefaultValue(1.00f)
            .setDescription("The crosshair opacity").build();

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
        float thick = thicknessConfig.getValue();
        float gap = gapConfig.getValue();

        boolean moving = InputUtil.isInputtingMovement() || mc.player.isSneaking() || mc.player.isClimbing() || !mc.player.isOnGround();
        if (dynamicConfig.getValue())
        {
            gapAnim.setState(moving);
            gap += 2.5f * (float) Easing.SMOOTH_STEP.ease(gapAnim.getFactor());
        }

        int fill = ColorUtil.withTransparency(ThemeModule.INSTANCE.getPrimaryColor().getRGB(), opacityConfig.getValue());
        int outline = ColorUtil.withTransparency(Colors.BLACK, opacityConfig.getValue());;
        boolean drawOutline = outlineConfig.getValue();
        float offset = outlineThicknessConfig.getValue();

        float hWidth = halfLength;
        float hHeight = thick * 2.0f;
        float vWidth = thick * 2.0f;
        float vHeight = halfLength;

        float lx = x - gap - halfLength;
        float ly = y - thick;
        float rx = x + gap;
        float ry = y - thick;

        float tx = x - thick;
        float ty = y - gap - halfLength;
        float bx = x - thick;
        float by = y + gap;

        if (drawOutline)
        {
            Managers.RENDER.drawOutline(context, lx, ly, hWidth, hHeight, offset, outline);
        }

        Managers.RENDER.drawRect(context, lx, ly, hWidth, hHeight, fill);

        if (drawOutline)
        {
            Managers.RENDER.drawOutline(context, rx, ry, hWidth, hHeight, offset, outline);
        }

        Managers.RENDER.drawRect(context, rx, ry, hWidth, hHeight, fill);

        if (drawOutline)
        {
            Managers.RENDER.drawOutline(context, tx, ty, vWidth, vHeight, offset, outline);
        }

        Managers.RENDER.drawRect(context, tx, ty, vWidth, vHeight, fill);

        if (drawOutline)
        {
            Managers.RENDER.drawOutline(context, bx, by, vWidth, vHeight, offset, outline);
        }

        Managers.RENDER.drawRect(context, bx, by, vWidth, vHeight, fill);
    }

    @Override
    public float getWidth()
    {
        return 0;
    }

    @Override
    public float getHeight()
    {
        return 0;
    }
}

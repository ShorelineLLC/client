package net.shoreline.client.gui.clickgui.config.picker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.*;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.animation.Smoother;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ColorPickerComponent extends ExpandableComponent<Color>
{
    private final ColorConfig colorConfig;
    private final Smoother colorSmootherX;
    private final Smoother colorSmootherY;
    private final Smoother hueSmootherY;
    private final Smoother alphaSmoother;

    private float[] selectedColor;
    private boolean draggingHue;
    private boolean draggingPicker;
    private boolean draggingTransparency;

    private final float pickerLength;

    private boolean pickerOpen;
    private final Animation collapseAnim;

    private final TextComponent hexComponent;

    public ColorPickerComponent(Config<Color> config,
                                ModuleComponent moduleComponent,
                                Frame frame,
                                float x,
                                float y,
                                float frameWidth,
                                float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.collapseAnim = new Animation(false, 200, Easing.CUBIC_OUT);
        this.colorConfig = (ColorConfig) config;
        this.colorSmootherX = new Smoother();
        this.colorSmootherY = new Smoother();
        this.hueSmootherY = new Smoother();
        this.alphaSmoother = new Smoother();
        this.pickerLength = width - 14;
        float[] hsb = colorConfig.getHsb();
        selectedColor = new float[] { hsb[0], hsb[1], hsb[2], hsb[3] };

        hexComponent = new TextComponent(frame, x, y, frameWidth, frameHeight,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                ch -> (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f'), // hex chars
                () -> "#" + Integer.toHexString(colorConfig.getRGB()),
                c -> colorConfig.setValue(new Color((int) Long.parseLong(c, 16), true)));

        frame.getAllComponents().add(hexComponent);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        boolean hovering = Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height);
        setHoverState(hovering);
        if (hovering)
        {
            ClickGuiScreen.INSTANCE.setDescriptionText(getConfig().getDescription());
        }

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        drawText(context, getConfig().getName(), getTx() + 3, getTy() + 4, theme.getTextColor());

        drawOutline(context, getTx() + getWidth() - 14, getTy() + 1.5f, 12, 12, 0.5f, theme.getColor(0x000000, 0.33f));
        drawBackground(context, getTx() + getWidth() - 14, getTy() + 1.5f, getTx() + getWidth() - 2, getTy() + 13.5f, 2, theme);
        drawRect(context, getTx() + getWidth() - 14, getTy() + 1.5f, 12, 12, theme.getColor(getConfig().getValue().getRGB(), 1.0f));

        if (collapseAnim.getFactor() > 0.001)
        {
            enableScissor(context, getTx() + 3, getTy() + height + 4, getTx() + width - 1, getTy() + height + getComponentHeight() + 2);

            for (int i = 0; i < pickerLength - 1; i++)
            {
                float hue = i / (float) pickerLength;
                drawRect(context, getTx() + pickerLength + 3, getTy() + i + height + 3, 11, 1, theme.getColor(Color.getHSBColor(hue, 1.0f, 1.0f).getRGB(), 1.0f));
            }
            // drawOutline(context, getTx() + pickerLength + 3, getTy() + height + 5, 10, pickerLength - 2, 1, Colors.BLACK);

            int clr = theme.getColor(Color.HSBtoRGB(colorConfig.getHsb()[0], 1.0f, 1.0f), 1.0f);
            int configColor = theme.getColor(new Color(colorConfig.getRGB(), false).getRGB(), 1.0f);
            drawGradientRect(context, getTx() + 2, getTy() + height + 4, getTx() + pickerLength, getTy() + height + pickerLength + 2, theme.getColor(0xFFFFFFFF, 1.0f), clr, true);
            drawGradientRect(context, getTx() + 2, getTy() + height + 4, getTx() + pickerLength, getTy() + height + pickerLength + 2, 0x00000000, theme.getColor(0xFF000000, 1.0f), false);

            drawOutline(context, getTx() + 3, getTy() + height + pickerLength + 6, pickerLength - 24, 13, 1, theme.getComponentColor());

            hexComponent.setYOffset(getYOffset());
            hexComponent.setX(getTx() + 3);
            hexComponent.setY(getTy() + height + pickerLength + 5);
            hexComponent.setWidth(pickerLength - 24);
            hexComponent.setHeight(13);
            hexComponent.drawComponent(context, mouseX, mouseY, delta);

            Identifier resetSprite = Identifier.of(ShorelineMod.MOD_ID, "icon/reset_clickgui.png");
            float resetX = getTx() + pickerLength - 18;
            float resetY = getTy() + height + pickerLength + 5;
            drawRect(context, resetX, resetY, 15, 15, theme.getComponentColor());
            drawTexturedRect(context, resetSprite, resetX + 2, resetY + 1, 12, 12, theme.getColor(-1, 1.0f));

            Identifier syncSprite = Identifier.of(ShorelineMod.MOD_ID, "icon/sync_clickgui.png");
            float syncX = getTx() + pickerLength;
            float syncY = getTy() + height + pickerLength + 5;
            drawRect(context, syncX, syncY, 15, 15, colorConfig.isGlobal() ? theme.getComponentColor() : theme.getColor(0xFFAAAAAA, 0.5f));
            drawTexturedRect(context, syncSprite, syncX, syncY + 1, 13, 13, theme.getColor(-1, 1.0f));

            if (colorConfig.isTransparency())
            {
                float alphaY = syncY + 17;
                drawBackground(context, getTx() + 2, alphaY, getTx() + 14 + pickerLength, alphaY + 15, 1, theme);
                drawGradientRect(context, getTx() + 2, alphaY, getTx() + 14 + pickerLength, alphaY + 15, configColor, ColorUtil.withTransparency(configColor, 0f), true);
            }

            drawSelectors(context, mouseX, mouseY, delta);
            disableScissor(context);
        }
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            pickerOpen = !pickerOpen;
            collapseAnim.setState(pickerOpen);
            collapseAnim.setEasing(pickerOpen ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
        }

        if (collapseAnim.getFactor() > 0.0)
        {
            hexComponent.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (Mouse.isHovering(mouseX, mouseY, getTx() + 2, getTy() + 2 + height + 4, pickerLength, pickerLength) && !colorConfig.isGlobal())
        {
            draggingPicker = true;
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + pickerLength + 4, getTy() + height + 3, 10, pickerLength - 2) && !colorConfig.isGlobal())
        {
            draggingHue = true;
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + 2, getTy() + height + pickerLength + 22, 14 + pickerLength, 15) && colorConfig.isTransparency())
        {
            draggingTransparency = true;
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + pickerLength, getTy() + height + pickerLength + 5, 15, 15))
        {
            colorConfig.setGlobal(!colorConfig.isGlobal());
            if (colorConfig.isGlobal())
            {
                colorConfig.setValue(new Color(ColorUtil.withTransparency(ThemeModule.INSTANCE.getPrimaryColor(), colorConfig.getAlpha() / 255f), true));
                selectedColor = colorConfig.getHsb();
            }
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + pickerLength - 18, getTy() + height + pickerLength + 5, 15, 15) && !colorConfig.isGlobal())
        {
            getConfig().setValue(getConfig().getDefaultValue());
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
        draggingHue = false;
        draggingPicker = false;
        draggingTransparency = false;
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            hexComponent.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            hexComponent.charTyped(chr, modifiers);
        }
    }

    @Override
    protected void onConfigUpdate(Color value)
    {
        hexComponent.updateBuffer(Integer.toHexString(value.getRGB()));
    }

    private void drawGradientRect(DrawContext context,
                                  float x1,
                                  float y1,
                                  float x2,
                                  float y2,
                                  int startColor,
                                  int endColor,
                                  boolean sideways)
    {
        float f = (startColor >> 24 & 255) / 255.0F;
        float f1 = (startColor >> 16 & 255) / 255.0F;
        float f2 = (startColor >> 8 & 255) / 255.0F;
        float f3 = (startColor & 255) / 255.0F;
        float f4 = (endColor >> 24 & 255) / 255.0F;
        float f5 = (endColor >> 16 & 255) / 255.0F;
        float f6 = (endColor >> 8 & 255) / 255.0F;
        float f7 = (endColor & 255) / 255.0F;
        Matrix4f posMatrix = context.getMatrices().peek().getPositionMatrix();

        VertexConsumer vc = ((IDrawContext) context).getVertexConsumerProvider().getBuffer(RenderLayer.getGui());
        if (sideways)
        {
            vc.vertex(posMatrix, x1, y1, 0.0F).color(f1, f2, f3, f);
            vc.vertex(posMatrix, x1, y2, 0.0F).color(f1, f2, f3, f);
            vc.vertex(posMatrix, x2, y2, 0.0F).color(f5, f6, f7, f4);
            vc.vertex(posMatrix, x2, y1, 0.0F).color(f5, f6, f7, f4);
        }
        else
        {
            vc.vertex(posMatrix, x2, y1, 0.0F).color(f1, f2, f3, f);
            vc.vertex(posMatrix, x1, y1, 0.0F).color(f1, f2, f3, f);
            vc.vertex(posMatrix, x1, y2, 0.0F).color(f5, f6, f7, f4);
            vc.vertex(posMatrix, x2, y2, 0.0F).color(f5, f6, f7, f4);
        }
    }

    private void drawBackground(DrawContext context, float x, float y, float x2, float y2, float size, Theme theme)
    {
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vc = ((IDrawContext) context)
                .getVertexConsumerProvider()
                .getBuffer(RenderLayer.getGui());

        drawRect(context, x, y, x2 - x, y2 - y, theme.getColor(0xFFFFFF, 1.0f));
        boolean skip = false;
        int clr = theme.getColor(0xFF808080, 1.0f);
        for (float yPos = y; yPos < y2; yPos += size)
        {
            skip = !skip;
            float startX = x + (skip ? size : 0);
            for (float xPos = startX; xPos < x2; xPos += size * 2)
            {
                if (xPos + size > x2 || yPos + size > y2)
                {
                    continue;
                }

                vc.vertex(matrix4f, xPos+ 0f, yPos + size, 0f).color(clr);
                vc.vertex(matrix4f, xPos + size, yPos + size, 0f).color(clr);
                vc.vertex(matrix4f, xPos + size, yPos + 0f, 0f).color(clr);
                vc.vertex(matrix4f, xPos + 0f, yPos + 0f, 0f).color(clr);
            }
        }
    }

    public void drawSelectors(DrawContext context, float mouseX, float mouseY, float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();
        int white = theme.getColor(0xFFFFFFFF, 1.0f);
        int black = theme.getColor(0xFF000000, 1.0f);

        float[] hsb = colorConfig.getHsb();
        float alpha = colorConfig.getAlpha() / 255f;
        float hueX = getTx() + pickerLength + 3;
        float hueW = 11;
        float hueY = getTy() + height + 3;
        float hueH = pickerLength - 1;

        float pickerX = getTx() + 2;
        float pickerY = getTy() + height + 4;
        float pickerW = pickerLength - 2;
        float pickerH = pickerLength - 2;

        float posX = pickerX + hsb[1] * pickerW;
        float posY = pickerY + (1.0f - hsb[2]) * pickerH;
        float smootherX = (float) colorSmootherX.smooth(posX, 0.5f, delta);
        float smootherY = (float) colorSmootherY.smooth(posY, 0.5f, delta);
        drawRect(context, smootherX - 2, smootherY - 2, 4, 4, black);
        drawRect(context, smootherX - 1, smootherY - 1, 2, 2, white);

        float hueSelectorY = hueY + hsb[0] * hueH;
        float hueSmoothY = (float) hueSmootherY.smooth(hueSelectorY - 2, 0.5f, delta);
        drawRect(context, hueX - 1, hueSmoothY, hueW + 2, 3, black);
        drawRect(context, hueX, hueSmoothY + 1, hueW + 1, 1, white);

        float alphaY = getTy() + height + pickerLength + 22;
        float alphaW = pickerLength + 10;
        if (colorConfig.isTransparency())
        {
            float alphaSelectorX = pickerX + (alphaW * (1.0f - alpha));
            float smootherSelector = (float) alphaSmoother.smooth(alphaSelectorX, 0.5f, delta);
            drawRect(context, smootherSelector - 1, alphaY - 1, 4, 17, black);
            drawRect(context, smootherSelector, alphaY, 2, 15, white);
        }

        if (draggingPicker && !colorConfig.isGlobal())
        {
            float sat = Math.max(0, Math.min(1, (mouseX - pickerX) / pickerW));
            float bri = 1.0f - Math.max(0, Math.min(1, (mouseY - pickerY) / pickerH));

            selectedColor[1] = sat;
            selectedColor[2] = bri;

            int color = Color.HSBtoRGB(selectedColor[0], selectedColor[1], selectedColor[2]);
            colorConfig.setValue(new Color(ColorUtil.withTransparency(color, colorConfig.getAlpha() / 255f), true));
        }

        if (draggingHue && !colorConfig.isGlobal())
        {
            float hue = Math.max(0, Math.min(1, (mouseY - hueY) / hueH));
            selectedColor[0] = hue;

            int color = Color.HSBtoRGB(selectedColor[0], selectedColor[1], selectedColor[2]);
            colorConfig.setValue(new Color(ColorUtil.withTransparency(color, colorConfig.getAlpha() / 255f), true));
        }

        if (draggingTransparency && colorConfig.isTransparency())
        {
            float transparency = Math.max(0, Math.min(1, 1.0f - (mouseX - pickerX) / alphaW));
            colorConfig.setValue(new Color(ColorUtil.withTransparency(colorConfig.getValue(), transparency), true));
        }
    }

    public float getComponentHeight()
    {
        float pickerHeight = pickerLength + 20.0f;
        if (colorConfig.isTransparency())
        {
            pickerHeight += 14.0f;
        }

        return (float) (pickerHeight * collapseAnim.getFactor());
    }
}
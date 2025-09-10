package net.shoreline.client.gui.clickgui.config.picker;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.*;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.*;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ColorPickerComponent extends ExpandableComponent<Color>
{
    private final ColorConfig colorConfig;
    private final Smoother colorSmootherX;
    private final Smoother colorSmootherY;
    private final Smoother hueSmootherY;

    private float[] selectedColor;
    private boolean draggingHue;
    private boolean draggingPicker;
    private boolean draggingTransparency;

    private final float pickerLength;

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
        this.colorConfig = (ColorConfig) config;
        this.colorSmootherX = new Smoother();
        this.colorSmootherY = new Smoother();
        this.hueSmootherY = new Smoother();
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
        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height));
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        drawText(context, getConfig().getName(), getTx() + 3, getTy() + 4, theme.getTextColor());

        drawOutline(context, getTx() + getWidth() - 14, getTy() + 2, 12, 12, 0.5f, 0x33000000);
        drawRect(context, getTx() + getWidth() - 14, getTy() + 2, 12, 12, getConfig().getValue().getRGB());

        if (collapseAnim.getFactor() > 0.001)
        {
            enableScissor(context, getTx() + 3, getTy() + height + 4, getTx() + width - 1, getTy() + height + getComponentHeight() + 2);

            for (int i = 0; i < pickerLength - 7; i++)
            {
                float hue = i / pickerLength;
                drawRect(context, getTx() + pickerLength, getTy() + i + height + 3, 12, 1, Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
            }
            // drawOutline(context, getTx() + pickerLength + 3, getTy() + height + 5, 10, pickerLength - 2, 1, Colors.BLACK);

            int configColor = new Color(colorConfig.getRGB(), false).getRGB();
            drawGradientRect(context, getTx() + 2, getTy() + height + 4, getTx() + pickerLength - 4, getTy() + height + pickerLength - 2, 0xffffffff, getConfig().getValue().getRGB(), true);
            drawGradientRect(context, getTx() + 2, getTy() + height + 4, getTx() + pickerLength - 4, getTy() + height + pickerLength - 2, 0x00000000, 0xff000000, false);

            drawOutline(context, getTx() + 3, getTy() + height + pickerLength + 3, pickerLength - 24, 13, 1, theme.getComponentColor());

            hexComponent.setYOffset(getYOffset());
            hexComponent.setX(getTx() + 3);
            hexComponent.setY(getTy() + height + pickerLength + 2);
            hexComponent.setWidth(pickerLength - 24);
            hexComponent.setHeight(13);
            hexComponent.drawComponent(context, mouseX, mouseY, delta);

            Identifier resetSprite = Identifier.of("shoreline", "icon/reset_clickgui.png");
            float resetX = getTx() + pickerLength - 18;
            float resetY = getTy() + height + pickerLength + 2;
            drawRect(context, resetX, resetY, 15, 15, theme.getComponentColor());
            drawTexturedRect(context, resetSprite, resetX + 1, resetY + 1, 12, 12);

            Identifier syncSprite = Identifier.of("shoreline", "icon/sync_clickgui.png");
            float syncX = getTx() + pickerLength - 1;
            float syncY = getTy() + height + pickerLength + 2;
            drawRect(context, syncX, syncY, 13, 15, ((ColorConfig) getConfig()).isGlobal() ? theme.getComponentColor() : 0xFFAAAAAA);
            drawTexturedRect(context, syncSprite, syncX, syncY + 1, 13, 13);

            float alphaY = syncY + 17;
            drawGradientRect(context, getTx() + 2, alphaY, getTx() + 14 + pickerLength, alphaY + 15, configColor, 0xFFFFFFFF, true);

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

        if (Mouse.isHovering(mouseX, mouseY, getTx() + 2, getTy() + 2 + height + 4, pickerLength, pickerLength))
        {
            draggingPicker = true;
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + pickerLength + 4, getTy() + height + 3, 10, pickerLength - 2))
        {
            draggingHue = true;
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + 2, getTy() + height + pickerLength + 22, 14 + pickerLength, 15))
        {
            draggingTransparency = true;
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + pickerLength, getTy() + height + pickerLength + 5, 15, 15))
        {
            ((ColorConfig) getConfig()).setGlobal(!((ColorConfig) getConfig()).isGlobal());
        }
        else if (Mouse.isHovering(mouseX, mouseY, getTx() + pickerLength - 18, getTy() + height + pickerLength + 5, 15, 15))
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

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        if (sideways)
        {
            bufferBuilder.vertex(posMatrix, x1, y1, 0.0F).color(f1, f2, f3, f);
            bufferBuilder.vertex(posMatrix, x1, y2, 0.0F).color(f1, f2, f3, f);
            bufferBuilder.vertex(posMatrix, x2, y2, 0.0F).color(f5, f6, f7, f4);
            bufferBuilder.vertex(posMatrix, x2, y1, 0.0F).color(f5, f6, f7, f4);
        }
        else
        {
            bufferBuilder.vertex(posMatrix, x2, y1, 0.0F).color(f1, f2, f3, f);
            bufferBuilder.vertex(posMatrix, x1, y1, 0.0F).color(f1, f2, f3, f);
            bufferBuilder.vertex(posMatrix, x1, y2, 0.0F).color(f5, f6, f7, f4);
            bufferBuilder.vertex(posMatrix, x2, y2, 0.0F).color(f5, f6, f7, f4);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public void drawSelectors(DrawContext context, float mouseX, float mouseY, float delta)
    {
        float[] hsb = colorConfig.getHsb();
        float alpha = colorConfig.getAlpha() / 255f;
        float hueX = getTx() + pickerLength + 1;
        float hueW = 10;
        float hueY = getTy() + height + 3;
        float hueH = pickerLength - 2;

        float pickerX = getTx() + 2;
        float pickerY = getTy() + height + 4;
        float pickerW = pickerLength - 2;
        float pickerH = pickerLength - 2;

        float posX = pickerX + hsb[1] * pickerW;
        float posY = pickerY + (1.0f - hsb[2]) * pickerH;
        float smootherX = (float) colorSmootherX.smooth(posX, 0.5f, delta);
        float smootherY = (float) colorSmootherY.smooth(posY, 0.5f, delta);
        drawRect(context, smootherX - 2, smootherY - 2, 4, 4, 0xFF000000);
        drawRect(context, smootherX - 1, smootherY - 1, 2, 2, 0xFFFFFFFF);

        float hueSelectorY = hueY + hsb[0] * hueH;
        float hueSmoothY = (float) hueSmootherY.smooth(hueSelectorY - 2, 0.5f, delta);
        drawRect(context, hueX - 1, hueSmoothY, hueW + 2, 3, 0xFF000000);
        drawRect(context, hueX, hueSmoothY + 1, hueW + 1, 1, 0xFFFFFFFF);

        float alphaY = getTy() + height + pickerLength + 22;
        float alphaW = pickerLength + 10;
        float alphaSelectorX = pickerX + (alphaW * (1.0f - alpha));
        drawRect(context, alphaSelectorX - 1, alphaY - 1, 4, 17, 0xFF000000);
        drawRect(context, alphaSelectorX, alphaY, 2, 15, 0xFFFFFFFF);

        if (draggingPicker)
        {
            float sat = Math.max(0, Math.min(1, (mouseX - pickerX) / pickerW));
            float bri = 1.0f - Math.max(0, Math.min(1, (mouseY - pickerY) / pickerH));

            selectedColor[1] = sat;
            selectedColor[2] = bri;
            colorConfig.setValue(new Color(Color.HSBtoRGB(selectedColor[0], selectedColor[1], selectedColor[2])));
        }

        if (draggingHue)
        {
            float hue = Math.max(0, Math.min(1, (mouseY - hueY) / hueH));
            selectedColor[0] = hue;
            colorConfig.setValue(new Color(Color.HSBtoRGB(selectedColor[0], selectedColor[1], selectedColor[2])));
        }

        if (draggingTransparency)
        {
            float transparency = Math.max(0, Math.min(1, 1.0f - (mouseX - pickerX) / alphaW));
            colorConfig.setValue(new Color(ColorUtil.withTransparency(colorConfig.getValue(), transparency), true));
        }
    }

    @Override
    public float getComponentHeight()
    {
        float scale = ClickGuiModule.INSTANCE.getScale();
        float pickerHeight = pickerLength + (20.0f * scale);
        if (colorConfig.isTransparency())
        {
            pickerHeight += 14.0f * scale;
        }

        return (float) (pickerHeight * collapseAnim.getFactor());
    }
}

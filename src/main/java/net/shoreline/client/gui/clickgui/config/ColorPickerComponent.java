package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.*;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.impl.render.*;
import net.shoreline.client.mixin.accessor.AccessorDrawContext;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ColorPickerComponent extends ConfigComponent<Color>
{
    private final ColorConfig colorConfig;
    private float[] selectedColor;

    private final int pickerLength;

    private boolean pickerOpen;
    private final Animation collapseAnim;

    private final TextComponent hexComponent;

    public ColorPickerComponent(Config<Color> config,
                                ModuleComponent moduleComponent,
                                Frame frame,
                                int x,
                                int y,
                                int frameWidth,
                                int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.collapseAnim = new Animation(false, 200, Easing.CUBIC_IN_OUT);
        this.colorConfig = (ColorConfig) config;
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

        drawText(context, textBuffer, Text.literal(getConfig().getName()).withColor(theme.getTextColor()), getTx() + 3, getTy() + 4);

        drawOutline(context, getTx() + getWidth() - 12, getTy() + 2, 12, 12, 1, 0x33000000);
        drawRect(context, getTx() + getWidth() - 12, getTy() + 2, 12, 12, getConfig().getValue().getRGB());

        if (collapseAnim.getFactor() > 0.0)
        {
            float[] hsb = colorConfig.getHsb();
            int color1 = Color.getHSBColor(hsb[0], 1.0f, 1.0f).getRGB();

            enableScissor(context, getTx() + 2, getTy() + height + 4, getTx() + width, getTy() + height + getComponentHeight() + 2);

            for (int i = 0; i < pickerLength - 2; i++)
            {
                float hue = i / (float) pickerLength;
                drawRect(context, getTx() + pickerLength + 4, getTy() + i + height + 3, 10, 1, Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
            }
            // drawOutline(context, getTx() + pickerLength + 3, getTy() + height + 5, 10, pickerLength - 2, 1, Colors.BLACK);

            drawGradientRect(context, getTx() + 2, getTy() + height + 4, getTx() + pickerLength, getTy() + height + pickerLength + 2, 0xffffffff, colorConfig.getRGB(), true);
            drawGradientRect(context, getTx() + 2, getTy() + height + 4, getTx() + pickerLength, getTy() + height + pickerLength + 2, 0x00000000, 0xff000000, false);

            drawOutline(context, getTx() + 3, getTy() + height + pickerLength + 6, pickerLength - 24, 13, 1, theme.getComponentColor());

            hexComponent.setYOffset(getYOffset());
            hexComponent.setX(getTx() + 3);
            hexComponent.setY(getTy() + height + pickerLength + 5);
            hexComponent.setWidth(pickerLength - 24);
            hexComponent.setHeight(13);
            hexComponent.drawComponent(context, mouseX, mouseY, delta);

            Identifier resetSprite = Identifier.of("shoreline", "icon/reset_clickgui.png");
            int resetX = getTx() + pickerLength - 18;
            int resetY = getTy() + height + pickerLength + 5;
            drawRect(context, resetX, resetY, 15, 15, theme.getComponentColor());
            drawTexturedRect(context, resetSprite, resetX + 2, resetY + 1, 12, 12);

            Identifier syncSprite = Identifier.of("shoreline", "icon/sync_clickgui.png");
            int syncX = getTx() + pickerLength;
            int syncY = getTy() + height + pickerLength + 5;
            drawRect(context, syncX, syncY, 15, 15, theme.getComponentColor());
            drawTexturedRect(context, syncSprite, syncX, syncY + 1, 13, 13);

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
        }

        if (collapseAnim.getFactor() > 0.0)
        {
            hexComponent.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

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
                                  int x1,
                                  int y1,
                                  int x2,
                                  int y2,
                                  int startColor,
                                  int endColor,
                                  boolean sideways)
    {
        Matrix3x2f matrices = new Matrix3x2f(context.getMatrices());
        DefaultGuiRenderState state = new DefaultGuiRenderState(
                RenderPipelines.GUI, TextureSetup.empty(), context,
                createBounds(context, x1, y1, x2, y2),
                (bb, z) ->
                {
                    float f = (startColor >> 24 & 255) / 255.0F;
                    float f1 = (startColor >> 16 & 255) / 255.0F;
                    float f2 = (startColor >> 8 & 255) / 255.0F;
                    float f3 = (startColor & 255) / 255.0F;
                    float f4 = (endColor >> 24 & 255) / 255.0F;
                    float f5 = (endColor >> 16 & 255) / 255.0F;
                    float f6 = (endColor >> 8 & 255) / 255.0F;
                    float f7 = (endColor & 255) / 255.0F;

                    DirectVertexConsumer bufferBuilder = new DirectVertexConsumer((BufferBuilder) bb, false);
                    if (sideways)
                    {
                        bufferBuilder.vertex(matrices, x1, y1, z).color(f1, f2, f3, f);
                        bufferBuilder.vertex(matrices, x1, y2, z).color(f1, f2, f3, f);
                        bufferBuilder.vertex(matrices, x2, y2, z).color(f5, f6, f7, f4);
                        bufferBuilder.vertex(matrices, x2, y1, z).color(f5, f6, f7, f4);
                    }
                    else
                    {
                        bufferBuilder.vertex(matrices, x2, y1, z).color(f1, f2, f3, f);
                        bufferBuilder.vertex(matrices, x1, y1, z).color(f1, f2, f3, f);
                        bufferBuilder.vertex(matrices, x1, y2, z).color(f5, f6, f7, f4);
                        bufferBuilder.vertex(matrices, x2, y2, z).color(f5, f6, f7, f4);
                    }
                });

        ((AccessorDrawContext) context).getState().addSimpleElement(state);
    }

    public int getComponentHeight()
    {
        int pickerHeight = pickerLength + 20;
        if (colorConfig.isTransparency())
        {
            pickerHeight += 14;
        }
        return (int) (pickerHeight * collapseAnim.getFactor());
    }
}

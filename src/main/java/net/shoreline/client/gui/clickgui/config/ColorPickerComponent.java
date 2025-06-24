package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.Theme;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ColorPickerComponent extends ConfigComponent<Color>
{
    private boolean pickerOpen;
    private final Animation collapseAnim;

    private float[] selectedColor;

    private final ColorConfig colorConfig;

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
        float[] hsb = colorConfig.getHsb();
        selectedColor = new float[] { hsb[0], hsb[1], 1.0f - hsb[2], hsb[3] };
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();
        drawText(context, Text.literal(getConfig().getName()).withColor(theme.getTextColor()), getTx() + 3, getTy() + 4);

        drawOutline(context, getTx() + getWidth() - 12, getTy() + 2, 12, 12, 1, 0x33000000);
        drawRect(context, getTx() + getWidth() - 12, getTy() + 2, 12, 12, getConfig().getValue().getRGB());
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
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {

    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {

    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {

    }
}

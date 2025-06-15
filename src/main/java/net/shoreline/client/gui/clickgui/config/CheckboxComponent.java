package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.Theme;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

public class CheckboxComponent extends ConfigComponent<Boolean>
{
    private final Animation toggleAnim;

    public CheckboxComponent(Config<Boolean> config,
                             ModuleComponent moduleComponent,
                             Frame frame,
                             int x,
                             int y,
                             int frameWidth,
                             int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.toggleAnim = new Animation(config.getValue(), 200L, Easing.CUBIC_IN_OUT);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int tx = getModuleComponent().getTx();
        int ty = getModuleComponent().getTy();
        drawRect(context, tx, ty, width, height, ColorUtil.withTransparency(theme.getComponentColor(), (float) toggleAnim.getFactor()));
        int textColor = ColorUtil.interpolateColor(1.0f - (float) toggleAnim.getFactor(), 0xffaaaaaa, theme.getTextColor());
        drawText(context, getConfig().getName(), tx + 3, ty + 4, textColor);
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isInBounds(mouseX, mouseY, getModuleComponent().getTx(), getModuleComponent().getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            boolean val = !getConfig().getValue();
            getConfig().setValue(val);
            toggleAnim.setState(val);
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

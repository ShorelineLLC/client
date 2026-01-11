package net.shoreline.client.gui.clickgui.components;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class ToggleComponent extends FrameComponent
{
    @Getter
    private final Animation toggleAnim;

    private final Supplier<Boolean> setter;

    public ToggleComponent(Frame frame,
                           float x,
                           float y,
                           float width,
                           float height,
                           boolean state,
                           Supplier<Boolean> setter)
    {
        super(frame, x, y, width, height);
        this.toggleAnim = new Animation(state, 150);
        this.setter = setter;
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, getX(), getY(), width, height));
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.withTransparency(theme.getComponentColor(), (float) toggleAnim.getFactor());
        color = ColorUtil.brighten(toggleAnim.getFactor() > 0.0 ? color : 0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getX(), getY(), width, height, color);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getX(), getY(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            toggleAnim.setState(setter.get());
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}

    @Override
    public void charTyped(char chr, int modifiers) {}

    public boolean getState()
    {
        return toggleAnim.getState();
    }

    public void setState(boolean state)
    {
        toggleAnim.setState(state);
    }

    public double getFactor()
    {
        return toggleAnim.getFactor();
    }
}

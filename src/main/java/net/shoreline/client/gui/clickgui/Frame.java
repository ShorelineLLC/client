package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Frame extends DrawableComponent implements Interactable
{
    private final String title;
    private int x, y;

    private int px, py;

    private int width;
    private int titleHeight;

    private int frameHeight;

    private boolean collapsed;
    private boolean dragging;

    // Components can be added inside the frame
    private final ComponentFactory componentFactory = new ComponentFactory();
    private final List<FrameComponent> components = new ArrayList<>();

    private final Animation collapseAnim = new Animation(true, 150L, Easing.CUBIC_IN_OUT);

    public Frame(String title, int x, int y, int width, int titleHeight)
    {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.titleHeight = titleHeight;
        this.frameHeight = width;
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {

        Mouse mouse = ClickGuiScreen.INSTANCE.getMouse();
        if (isDragging())
        {
            x += mouse.getMouseX() - px;
            y += mouse.getMouseY() - py;
        }

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        drawOutline(context, x + 1, y + 1, width - 2, titleHeight + (int) (frameHeight * collapseAnim.getFactor()) - 2, 1, theme.getOutlineColor());
        drawRect(context, x, y, width, titleHeight, theme.getBackgroundColor());
        drawRect(context, x, y, width, titleHeight, theme.getTitleColor());
        drawText(context, title, x + 3, y + 5, -1);

        if (collapseAnim.getFactor() > 0.0)
        {
            context.enableScissor(x, y + titleHeight, x + width, y + titleHeight + (int) (frameHeight * collapseAnim.getFactor()));
            drawRect(context, x, y + titleHeight, width, frameHeight, theme.getBackgroundColor());

            for (FrameComponent component : components)
            {
                component.drawComponent(context, mouseX, mouseY, delta);
            }

            context.disableScissor();
        }

        px = mouse.getMouseX();
        py = mouse.getMouseY();
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isInBounds(mouseX, mouseY, x, y, width, titleHeight)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.collapsed = !collapsed;
            collapseAnim.setState(collapsed);
        }

        for (FrameComponent component : components)
        {
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

        for (FrameComponent component : components)
        {
            component.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {
        for (FrameComponent component : components)
        {
            component.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        for (FrameComponent component : components)
        {
            component.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        for (FrameComponent component : components)
        {
            component.charTyped(chr, modifiers);
        }
    }

    protected void addComponent(FrameComponent component)
    {
        components.add(component);
    }
}

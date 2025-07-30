package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
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

    private boolean frameOpen;
    private boolean dragging;

    private final Animation collapseAnim;

    // Components can be added inside the frame
    private final ComponentFactory componentFactory = new ComponentFactory();
    protected final List<FrameComponent> components = new ArrayList<>();
    protected final List<FrameComponent> allComponents = new ArrayList<>();

    public Frame(String title, int x, int y, int width, int titleHeight)
    {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.titleHeight = titleHeight;
        this.frameOpen = true;
        this.collapseAnim = new Animation(true, 150L, Easing.CUBIC_IN_OUT);
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

        int frameHeight = getComponentHeight();
        drawOutline(context, x + 1, y + 1, width - 2, titleHeight + (int) (frameHeight * collapseAnim.getFactor()) - 2, 1, theme.getOutlineColor());
        drawRect(context, x, y, width, titleHeight, theme.getBackgroundColor());
        drawRect(context, x, y, width, titleHeight, theme.getTitleColor());
        drawText(context, textBuffer, Text.literal(title).withColor(theme.getTextColor()), x + 3, y + 5);

        if (collapseAnim.getFactor() > 0.0)
        {
            enableScissor(context, x, y + titleHeight, x + width, y + titleHeight + (int) (frameHeight * collapseAnim.getFactor()));
            drawRect(context, x, y + titleHeight, width, frameHeight, theme.getBackgroundColor());

            int yOffset = 0;
            for (FrameComponent component : components)
            {
                component.setYOffset(yOffset);
                component.drawComponent(context, mouseX, mouseY, delta);
                if (component instanceof ModuleComponent component1)
                {
                    if (component instanceof ModuleComponent c1)
                    {
                        yOffset += c1.getScaledHeight();
                    }
                }
            }

            disableScissor(context);
        }

        px = mouse.getMouseX();
        py = mouse.getMouseY();
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, x, y, width, titleHeight)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.frameOpen = !frameOpen;
            collapseAnim.setState(frameOpen);
            collapseAnim.setEasing(frameOpen ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
        }

        if (collapseAnim.getFactor() > 0.0)
        {
            for (FrameComponent component : components)
            {
                component.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (FrameComponent component : components)
            {
                component.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (FrameComponent component : components)
            {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (FrameComponent component : components)
            {
                component.charTyped(chr, modifiers);
            }
        }
    }

    public int getComponentHeight()
    {
        int frameHeight = 2;
        for (FrameComponent component : components)
        {
            int height = component.getHeight() + 1;
            if (component instanceof ModuleComponent c1)
            {
                height += c1.getScaledHeight();
            }
            frameHeight += height;
        }
        return frameHeight;
    }
}

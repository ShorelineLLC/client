package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

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

        drawRect(context, x, y, width, titleHeight, theme.getTitleColor());
        drawText(context, title, x + 3, y + 4, -1);

        drawRect(context, x, y + titleHeight, width, frameHeight, theme.getBackgroundColor());

        px = mouse.getMouseX();
        py = mouse.getMouseY();
    }


    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
        if (!isCollapsed())
        {
            setDragging(false);
        }

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

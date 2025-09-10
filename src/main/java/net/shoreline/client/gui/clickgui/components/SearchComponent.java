package net.shoreline.client.gui.clickgui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

public class SearchComponent extends FrameComponent
{
    private String searchText = "";
    private final TextComponent searchComponent;

    public SearchComponent(Frame frame,
                           float x,
                           float y,
                           float width,
                           float height)
    {
        super(frame, x, y, width, height);

        this.searchComponent = new TextComponent(
                frame, x, y, width, height,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                () -> searchText,
                c -> searchText = c
        );

        frame.getAllComponents().add(searchComponent);
    }

    public String getQuery()
    {
        return searchText;
    }

    public void setQuery(String query)
    {
        if (query == null)
        {
            query = "";
        }

        this.searchText = query;
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int outline = ColorUtil.brighten(theme.getComponentColor(), searchComponent.isTyping() ? 80 : 30, (float) hoverAnim.getFactor());

        drawOutline(context, getX(), getY(), width, height, 1.0f, outline);

        float textX = getX() + 3.0f;
        float textY = getY() + 4.0f;

        searchComponent.setX(getX());
        searchComponent.setY(getY());
        searchComponent.setWidth(width);
        searchComponent.setHeight(height);
        searchComponent.setYOffset(getYOffset());

        if (searchComponent.isTyping())
        {
            searchComponent.drawComponent(context, mouseX, mouseY, delta);
        }
        else
        {
            searchComponent.updateBuffer("");
            drawText(context, Formatting.GRAY + "Search...", textX, textY, theme.getTextColor());
        }
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        searchComponent.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
        searchComponent.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        searchComponent.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        searchComponent.charTyped(chr, modifiers);
    }
}

package net.shoreline.client.gui.titlescreen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.font.FontRenderer;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.render.animation.ColorAnimation;

import java.awt.*;

@AllArgsConstructor
@Getter
public class MenuButton
{
    private static final FontRenderer RENDERER = FontManager.FONT_RENDERER;
    private final ColorAnimation animation = new ColorAnimation(150);
    private final String name;
    private final Runnable runnable;
    private final float x;
    private final float y;

    public void render(DrawContext context, double mouseX, double mouseY, float delta)
    {
        boolean hovered = Mouse.isHovering(mouseX, mouseY, getX(), getY() - 2, getWidth(), RENDERER.getFontHeight() + 2);
        animation.setState(hovered);

        int color = animation.getColor(new Color(128, 128, 128, 128), Color.WHITE).getRGB();
        RENDERER.drawString(context, name, x, y, color);
    }

    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (Mouse.isHovering(mouseX, mouseY, getX(), getY(), getWidth(), RENDERER.getFontHeight()) && button == 0)
        {
            runnable.run();
        }
    }

    public float getWidth()
    {
        return RENDERER.getStringWidth(name);
    }
}
package net.shoreline.client.gui.clickgui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.util.Window;

public abstract class DrawableComponent
{
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public abstract void drawComponent(DrawContext context,
                                       float mouseX,
                                       float mouseY,
                                       float delta);

    protected void drawRect(DrawContext context,
                            int x,
                            int y,
                            int width,
                            int height,
                            int color)
    {
        context.fill(x, y, x + width, y + height, color);
    }

    protected void drawText(DrawContext context,
                            String text,
                            int x,
                            int y,
                            int color,
                            boolean shadow)
    {
        context.drawText(mc.textRenderer, text, x, y, color, shadow);
    }

    protected void drawText(DrawContext context,
                            String text,
                            int x,
                            int y,
                            int color)
    {
        context.drawText(mc.textRenderer, text, x, y, color, true);
    }

    /** @see net.shoreline.client.gui.ScissorStack **/

    protected void enableScissor(int x, int y, int x1, int y1)
    {
        ScreenRect rect = new ScreenRect(x, y, x1 - x, y1 - y);
        ClickGuiScreen.INSTANCE.getScissorStack().push(rect);
        setScissor(rect);
    }

    protected void disableScissor()
    {
        ScreenRect rect = ClickGuiScreen.INSTANCE.getScissorStack().pop();
        setScissor(rect);
    }

    private void setScissor(ScreenRect rect)
    {
        if (rect != null)
        {
            Window window = mc.getWindow();
            int i = window.getFramebufferHeight();
            double d = window.getScaleFactor();
            double e = (double) rect.getLeft() * d;
            double f = (double) i - (double) rect.getBottom() * d;
            double g = (double) rect.width() * d;
            double h = (double) rect.height() * d;
            RenderSystem.enableScissor((int) e, (int) f, Math.max(0, (int) g), Math.max(0, (int) h));
        } else
        {
            RenderSystem.disableScissor();
        }
    }
}

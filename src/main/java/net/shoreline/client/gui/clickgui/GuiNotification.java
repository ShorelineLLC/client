package net.shoreline.client.gui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ColorUtil;

public class GuiNotification
{
    private final String notification;
    private final float length;
    private final int duration;
    private Long start;

    public GuiNotification(String notification, int duration)
    {
        this.notification = notification;
        this.length = Managers.RENDER.getTextWidth(notification) / 2f;
        this.duration = duration;
        this.start = 0L;
    }

    public void render(DrawContext context)
    {
        float alpha = getAlpha();
        Managers.RENDER.drawText(context.getMatrices(), notification, (context.getScaledWindowWidth() / 2f) - length, context.getScaledWindowHeight() - 40, ColorUtil.withTransparency(-1, alpha));
    }

    public boolean isExpired()
    {
        if (start == 0)
        {
            start = System.currentTimeMillis();
        }

        long elapsed = System.currentTimeMillis() - start;
        return elapsed >= duration + 500;
    }

    public float getAlpha()
    {
        float alpha;
        int fade = 250;
        long delta = System.currentTimeMillis() - start;
        if (delta < fade)
        {
            alpha = delta / (float) fade;
        }
        else if (delta < fade + duration)
        {
            alpha = 1f;
        }
        else if (delta < fade + duration + fade)
        {
            long elapsed = delta - (fade + duration);
            alpha = 1f - (elapsed / (float) fade);
        }
        else
        {
            alpha = 0f;
        }

        return Math.max(0f, Math.min(1f, alpha));
    }
}
package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class ServerStatusHudModule extends HudModule
{
    private final Timer serverStatus = new NanoTimer();
    private final Animation statusAnimation = new Animation(300L);

    public ServerStatusHudModule()
    {
        super("ServerStatus", "Displays the status of the server", 400, 100);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        statusAnimation.setState(serverStatus.hasPassed(1100));
        int color = ColorUtil.interpolateColor(MathHelper.clamp((serverStatus.getElapsedTime() - 1000.0f) / 5000.0f, 0.0f, 1.0f), Colors.RED, Colors.GREEN);
        if (statusAnimation.getFactor() >= 0.01f)
        {
            drawText(context.getMatrices(), getStatusText(), getX() + 2, getY() + 2, ColorUtil.withTransparency(color, (float) statusAnimation.getFactor()));
        }
    }

    @Override
    public void drawGuiComponent(DrawContext context, float tickDelta)
    {
        drawText(context.getMatrices(), getStatusText(), getX() + 2, getY() + 2, Colors.GREEN);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        serverStatus.reset();
    }

    @Override
    public float getWidth()
    {
        return getTextWidth(getStatusText());
    }

    @Override
    public float getHeight()
    {
        return 12;
    }

    private String getStatusText()
    {
        return String.format(Formatting.WHITE + "Server not responding " + Formatting.GRAY + "(" + Formatting.RESET + "%s.s" + Formatting.GRAY + ")",
                DECIMAL.format(serverStatus.getElapsedTime() / 1000.0));
    }
}

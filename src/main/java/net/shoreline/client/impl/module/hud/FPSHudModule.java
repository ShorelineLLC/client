package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.util.math.PerSecond;

public class FPSHudModule extends HudModule
{
    private final PerSecond fps = new PerSecond();

    public FPSHudModule()
    {
        super("FPS", "Displays current game FPS", 200, 200);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        fps.count();
        drawText(context.getMatrices(), getFPSText(), getX() + 2, getY() + 2);
    }

    @Override
    public int getWidth()
    {
        return getTextWidth(getFPSText());
    }

    @Override
    public int getHeight()
    {
        return 12;
    }

    public Text getFPSText()
    {
        return Text.literal("FPS ").append(String.valueOf(fps.getPerSecond()));
    }
}

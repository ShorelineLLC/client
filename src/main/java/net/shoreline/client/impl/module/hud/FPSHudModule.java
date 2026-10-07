package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.util.math.PerSecond;

public class FPSHudModule extends DynamicHudModule
{
    private final PerSecond fps = new PerSecond();

    public FPSHudModule()
    {
        super("FPS", "Displays current game FPS", 200, 200);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getFPSText, () -> true));
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        super.drawHudComponent(context, tickDelta);
        fps.count();
    }

    public String getFPSText()
    {
        return "FPS " + Formatting.WHITE + fps.getPerSecond();
    }
}

package net.shoreline.client.impl.module.impl;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;

public abstract class HudModule extends RenderModule
{
    Config<Integer> x = new NumberConfig.Builder<Integer>("X-Position").setDefaultValue(0).build();
    Config<Integer> y = new NumberConfig.Builder<Integer>("Y-Position").setDefaultValue(0).build();

    public HudModule(String name, String description, int x, int y)
    {
        super(name, description, GuiCategory.HUD);
        this.x.setValue(x);
        this.y.setValue(y);
        unregisterConfig(getKeybind());
    }

    public abstract void drawHudComponent(DrawContext context, float tickDelta);

    public void drawGuiComponent(DrawContext context, float tickDelta)
    {
        drawHudComponent(context, tickDelta);
    }

    @Override
    public String getId()
    {
        return String.format("%s_hud_module", getName().toLowerCase());
    }

    public int getX()
    {
        return x.getValue();
    }

    public int getY()
    {
        return y.getValue();
    }

    public void setX(int x)
    {
        this.x.setValue(x);
    }

    public void setY(int y)
    {
        this.y.setValue(y);
    }

    public abstract int getWidth();

    public abstract int getHeight();
}

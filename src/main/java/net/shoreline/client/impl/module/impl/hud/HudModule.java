package net.shoreline.client.impl.module.impl.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.gui.hud.Anchor;
import net.shoreline.client.impl.module.client.HudGuiModule;
import net.shoreline.client.impl.module.impl.RenderModule;

public abstract class HudModule extends RenderModule
{
    Config<Float> x = new NumberConfig.Builder<Float>("X-Position")
            .setDefaultValue(0f)
            .setVisible(() -> false).build();
    Config<Float> y = new NumberConfig.Builder<Float>("Y-Position")
            .setDefaultValue(0f)
            .setVisible(() -> false).build();
    Config<Integer> index = new NumberConfig.Builder<Integer>("Index")
            .setMin(0).setMax(100).setDefaultValue(0)
            .setVisible(() -> false).build();
    Config<Anchor> anchor = new EnumConfig.Builder<Anchor>("Anchor")
            .setValues(Anchor.values())
            .setDefaultValue(Anchor.None)
            .setVisible(() -> false).build();

    public HudModule(String name, String description, float x, float y)
    {
        super(name, description, GuiCategory.HUD);
        this.x.setValue(x);
        this.y.setValue(y);
        unregisterConfig(getKeybind());
        registerConfigs(this.x, this.y, index, anchor);
    }

    public abstract void drawHudComponent(DrawContext context, float tickDelta);

    public void drawGuiComponent(DrawContext context, float tickDelta)
    {
        // context.enableScissor(getX() - 3, getY(), getX() + getWidth() + 3, getY() + getHeight());
        drawHudComponent(context, tickDelta);
        // context.disableScissor();
    }

    @Override
    public String getId()
    {
        return String.format("%s_hud_module", getName().toLowerCase());
    }

    public float getX()
    {
        return x.getValue();
    }

    public float getY()
    {
        return y.getValue();
    }

    public void setX(float x)
    {
        this.x.setValue(x);
    }

    public void setY(float y)
    {
        this.y.setValue(y);
    }

    public Anchor getAnchor()
    {
        return anchor.getValue();
    }

    public void setAnchor(Anchor anchor)
    {
        this.anchor.setValue(anchor);
    }

    public int getIndex()
    {
        return index.getValue();
    }

    public void setIndex(int index)
    {
        this.index.setValue(index);
    }

    public abstract float getWidth();

    public abstract float getHeight();

    @Override
    public void drawText(MatrixStack matrices, String text, float x, float y)
    {
        int color = HudGuiModule.INSTANCE.getColor((int) y);
        drawText(matrices, text, x, y, color);
    }
}

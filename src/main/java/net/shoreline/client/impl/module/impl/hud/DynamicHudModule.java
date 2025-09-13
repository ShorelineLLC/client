package net.shoreline.client.impl.module.impl.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.module.client.HudGuiModule;
import net.shoreline.client.impl.render.ColorUtil;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public abstract class DynamicHudModule extends HudModule
{
    protected final List<DynamicEntry> hudEntries = new CopyOnWriteArrayList<>();
    protected float offset = 0;
    protected float width = 0;
    protected boolean left;
    protected boolean top;

    public DynamicHudModule(String name, String description, int x, int y)
    {
        super(name, description, x, y);
    }

    @Override
    public void onDisable()
    {
        hudEntries.clear();
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        drawEntries(context, tickDelta);
        cacheWidth();

        float center = mc.getWindow().getScaledHeight() / 2f;
        top  = !(getY() + (getHeight() / 2.0f) > center);
        left = getX() + (getWidth() / 2f) < mc.getWindow().getScaledWidth() / 2f;
    }

    @Override
    public float getWidth()
    {
        return width;
    }

    @Override
    public float getHeight()
    {
        return offset;
    }

    public void drawEntries(DrawContext context, float tickDelta)
    {
        offset = 0;
        sortEntries();
        for (DynamicEntry entry : getHudEntries())
        {
            if (entry.isDrawing() || !entry.isDone())
            {
                entry.draw(context, getX() + (isLeft() ? 0 : getWidth()), getY(), offset, tickDelta);
            }
        }
    }

    public void sortEntries()
    {
        boolean top = isTop();
        getHudEntries().sort(Comparator.comparingDouble(
                entry -> getTextWidth(entry.getText().get()) * (top ? -1 : 1)));
    }

    public void cacheWidth()
    {
        int result = 0;
        for (DynamicEntry entry : getHudEntries())
        {
            if (entry.isDrawing() || !entry.isDone())
            {
                result = Math.max(result, getTextWidth(entry.getText().get()));
            }
        }

        width = result;
    }

    public void drawTextTransparency(MatrixStack matrices, String text, float x, float y, float transparency)
    {
        int color = ColorUtil.withTransparency(HudGuiModule.INSTANCE.getColor((int) y), transparency);
        drawText(matrices, text, x, y, color);
    }
}

package net.shoreline.client.impl.module.impl.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public abstract class DynamicHudModule extends HudModule
{
    protected final List<DynamicEntry> hudEntries = new ArrayList<>();
    protected int offset = 0;
    protected int width = 0;

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
        drawEntries(context);
        cacheWidth();
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return offset;
    }

    public void drawEntries(DrawContext context)
    {
        offset = 0;
        sortEntries();
        for (DynamicEntry entry : getHudEntries())
        {
            if (entry.isDrawing() || !entry.isDone())
            {
                entry.draw(context, getX() + (isLeft() ? 0 : getWidth()), getY(), offset);
            }
        }
    }

    public void sortEntries()
    {
        boolean top = isTop();
        getHudEntries().sort(Comparator.comparingDouble(
                entry -> getTextWidth(entry.getText().get()) * (top ? -1 : 1)));
    }

    public boolean isLeft()
    {
        return getX() + (getWidth() / 2f) < mc.getWindow().getScaledWidth() / 2f;
    }

    public boolean isTop()
    {
        float center = mc.getWindow().getScaledHeight() / 2f;
        return !(getY() + (getHeight() / 2.0f) > center);
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
}

package net.shoreline.client.impl.module.impl;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.render.FreecamModule;

public class RenderModule extends Toggleable
{
    public RenderModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public RenderModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected Vec3d getCameraPos()
    {
        return FreecamModule.INSTANCE.isEnabled() ? FreecamModule.INSTANCE.getPosition() : mc.player.getPos();
    }

    public void drawText(MatrixStack matrices, String text, float x, float y)
    {
        drawText(matrices, text, x, y, -1);
    }

    public void drawText(MatrixStack matrices, String text, float x, float y, int color)
    {
        Managers.RENDER.drawText(matrices, text, x, y, color);
    }

    public int getTextWidth(String text)
    {
        return (int) Managers.RENDER.getTextWidth(text);
    }

    protected void reload(boolean soft)
    {
        if (mc.worldRenderer == null)
        {
            return;
        }

        if (soft && mc.player != null)
        {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            int d = mc.options.getViewDistance().getValue() * 16;
            mc.worldRenderer.scheduleBlockRenders(x - d, y - d, z - d, x + d, y + d, z + d);
        } else
        {
            mc.worldRenderer.reload();
        }
    }
}

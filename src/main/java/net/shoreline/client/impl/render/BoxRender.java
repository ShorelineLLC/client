package net.shoreline.client.impl.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.shoreline.client.impl.Managers;

public enum BoxRender
{
    FILL
    {
        @Override
        public void render(MatrixStack matrices, Box box, int color)
        {
            Managers.RENDER.renderBoundingBox(matrices, box, ColorUtil.withTransparency(color, 0.75f));
            Managers.RENDER.renderBox(matrices, box, ColorUtil.withTransparency(color, 0.3f));
        }
    },
    OUTLINE
    {
        @Override
        public void render(MatrixStack matrices, Box box, int color)
        {
            Managers.RENDER.renderBoundingBox(matrices, box, ColorUtil.withTransparency(color, 0.75f));
        }
    };

    public abstract void render(MatrixStack matrices, Box box, int color);
}

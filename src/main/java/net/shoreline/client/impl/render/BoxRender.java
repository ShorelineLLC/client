package net.shoreline.client.impl.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.shoreline.client.impl.Managers;

import java.awt.*;

public enum BoxRender
{
    FILL
    {
        @Override
        public void render(MatrixStack matrices, Box box, int color, float transparency)
        {
            Color color1 = new Color(color, false);
            Managers.RENDER.renderBoundingBox(matrices, box, ColorUtil.withTransparency(color1, transparency));
            Managers.RENDER.renderBox(matrices, box, ColorUtil.withTransparency(color1, 0.3f * transparency));
        }
    },
    OUTLINE
    {
        @Override
        public void render(MatrixStack matrices, Box box, int color, float transparency)
        {
            Color color1 = new Color(color, false);
            Managers.RENDER.renderBoundingBox(matrices, box, ColorUtil.withTransparency(color1, transparency));
        }
    };

    public void render(MatrixStack matrices, BlockPos blockPos, int color)
    {
        render(matrices, new Box(blockPos), color);
    }

    public void render(MatrixStack matrices, BlockPos blockPos, int color, float transparency)
    {
        render(matrices, new Box(blockPos), color, transparency);
    }

    public void render(MatrixStack matrices, Box box, int color)
    {
        render(matrices, box, color, 1.0f);
    }

    public abstract void render(MatrixStack matrices, Box box, int color, float transparency);

}

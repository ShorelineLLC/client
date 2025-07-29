package net.shoreline.client.impl.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.opengl.GL11;

@Getter
@Setter
public class RenderManager
{
    private double deltaTime;

    public void startRender()
    {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public void endRender()
    {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public void renderBox(MatrixStack matrixStack,
                          VertexConsumerProvider.Immediate vertexConsumers,
                          BlockPos blockPos,
                          int color)
    {
        renderBox(matrixStack, vertexConsumers, new Box(blockPos), color);
    }

    public void renderBox(MatrixStack matrixStack,
                          VertexConsumerProvider.Immediate vertexConsumers,
                          Box box,
                          int color)
    {

        float[] rgb = ColorUtil.getRGBValues(color);
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        VertexRendering.drawFilledBox(matrixStack, consumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, rgb[0], rgb[1], rgb[2], rgb[3]);

        VertexConsumer consumer1 = vertexConsumers.getBuffer(RenderLayer.getLines());
        VertexRendering.drawOutline(matrixStack, consumer1, VoxelShapes.cuboid(box), 0.0f, 0.0f, 0.0f, color);
    }
}

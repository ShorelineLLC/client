package net.shoreline.client.impl.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Getter
@Setter
public class RenderManager
{
    private double deltaTime;

    public void renderBox(MatrixStack matrixStack,
                          BlockPos blockPos,
                          int color)
    {
        renderBox(matrixStack, new Box(blockPos), color);
    }

    public void renderBox(MatrixStack matrixStack,
                          Box box,
                          int color)
    {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();
        float minX = (float) (box.minX - camera.getX());
        float minY = (float) (box.minY - camera.getY());
        float minZ = (float) (box.minZ - camera.getZ());
        float maxX = (float) (box.maxX - camera.getX());
        float maxY = (float) (box.maxY - camera.getY());
        float maxZ = (float) (box.maxZ - camera.getZ());

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, minX, minY, minZ).color(color);
        buffer.vertex(matrix, maxX, minY, minZ).color(color);
        buffer.vertex(matrix, maxX, minY, maxZ).color(color);
        buffer.vertex(matrix, minX, minY, maxZ).color(color);

        buffer.vertex(matrix, minX, maxY, minZ).color(color);
        buffer.vertex(matrix, minX, maxY, maxZ).color(color);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(color);
        buffer.vertex(matrix, maxX, maxY, minZ).color(color);

        buffer.vertex(matrix, minX, minY, minZ).color(color);
        buffer.vertex(matrix, minX, maxY, minZ).color(color);
        buffer.vertex(matrix, maxX, maxY, minZ).color(color);
        buffer.vertex(matrix, maxX, minY, minZ).color(color);

        buffer.vertex(matrix, maxX, minY, minZ).color(color);
        buffer.vertex(matrix, maxX, maxY, minZ).color(color);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(color);
        buffer.vertex(matrix, maxX, minY, maxZ).color(color);

        buffer.vertex(matrix, minX, minY, maxZ).color(color);
        buffer.vertex(matrix, maxX, minY, maxZ).color(color);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(color);
        buffer.vertex(matrix, minX, maxY, maxZ).color(color);

        buffer.vertex(matrix, minX, minY, minZ).color(color);
        buffer.vertex(matrix, minX, minY, maxZ).color(color);
        buffer.vertex(matrix, minX, maxY, maxZ).color(color);
        buffer.vertex(matrix, minX, maxY, minZ).color(color);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public void renderBoundingBox(MatrixStack matrixStack, BlockPos pos, int color)
    {
        renderBoundingBox(matrixStack, new Box(pos), color);
    }

    public void renderBoundingBox(MatrixStack matrixStack, Box box, int color)
    {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();
        float minX = (float) (box.minX - camera.getX());
        float minY = (float) (box.minY - camera.getY());
        float minZ = (float) (box.minZ - camera.getZ());
        float maxX = (float) (box.maxX - camera.getX());
        float maxY = (float) (box.maxY - camera.getY());
        float maxZ = (float) (box.maxZ - camera.getZ());

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, minX, minY, minZ).color(color);
        buffer.vertex(matrix, minX, minY, maxZ).color(color);
        buffer.vertex(matrix, minX, minY, maxZ).color(color);
        buffer.vertex(matrix, maxX, minY, maxZ).color(color);

        buffer.vertex(matrix, maxX, minY, maxZ).color(color);
        buffer.vertex(matrix, maxX, minY, minZ).color(color);
        buffer.vertex(matrix, maxX, minY, minZ).color(color);
        buffer.vertex(matrix, minX, minY, minZ).color(color);

        buffer.vertex(matrix, minX, maxY, minZ).color(color);
        buffer.vertex(matrix, minX, maxY, maxZ).color(color);
        buffer.vertex(matrix, minX, maxY, maxZ).color(color);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(color);

        buffer.vertex(matrix, maxX, maxY, maxZ).color(color);
        buffer.vertex(matrix, maxX, maxY, minZ).color(color);
        buffer.vertex(matrix, maxX, maxY, minZ).color(color);
        buffer.vertex(matrix, minX, maxY, minZ).color(color);

        buffer.vertex(matrix, minX, minY, minZ).color(color);
        buffer.vertex(matrix, minX, maxY, minZ).color(color);
        buffer.vertex(matrix, maxX, minY, minZ).color(color);
        buffer.vertex(matrix, maxX, maxY, minZ).color(color);

        buffer.vertex(matrix, maxX, minY, maxZ).color(color);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(color);
        buffer.vertex(matrix, minX, minY, maxZ).color(color);
        buffer.vertex(matrix, minX, maxY, maxZ).color(color);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}

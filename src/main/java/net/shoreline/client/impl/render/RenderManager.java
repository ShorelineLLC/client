package net.shoreline.client.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.module.client.FontModule;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

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
        startRender();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();
        float minX = (float) (box.minX - camera.getX());
        float minY = (float) (box.minY - camera.getY());
        float minZ = (float) (box.minZ - camera.getZ());
        float maxX = (float) (box.maxX - camera.getX());
        float maxY = (float) (box.maxY - camera.getY());
        float maxZ = (float) (box.maxZ - camera.getZ());

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
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
        endRender();
    }

    public void renderBoundingBox(MatrixStack matrixStack, BlockPos pos, int color)
    {
        renderBoundingBox(matrixStack, new Box(pos), color);
    }

    public void renderBoundingBox(MatrixStack matrixStack, Box box, int color)
    {
        startRender();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();
        float minX = (float) (box.minX - camera.getX());
        float minY = (float) (box.minY - camera.getY());
        float minZ = (float) (box.minZ - camera.getZ());
        float maxX = (float) (box.maxX - camera.getX());
        float maxY = (float) (box.maxY - camera.getY());
        float maxZ = (float) (box.maxZ - camera.getZ());

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
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
        endRender();
    }

    public void renderNametag(MatrixStack matrixStack, Vec3d pos, float scale, String text, int color)
    {
        EntityRenderDispatcher entityRenderer =MinecraftClient.getInstance().getEntityRenderDispatcher();
        Camera camera = entityRenderer.camera;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(1.0f, -32500000);

        float distance = (float) Math.sqrt(camera.getPos().squaredDistanceTo(pos));
        float scaling = 0.0018f + scale * distance;
        if (distance <= 8.0)
        {
            scaling = 0.0245f;
        }

        pos = pos.subtract(camera.getPos());
        matrixStack.push();
        matrixStack.translate(pos);
        matrixStack.multiply(entityRenderer.getRotation());
        matrixStack.scale(scaling, -scaling, scaling);

        float hwidth = getTextWidth(text) / 2.0f;
        drawText(matrixStack, text, (int) -hwidth, 0, color);

        matrixStack.pop();

        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(1.0f, 32500000);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public void drawText(MatrixStack matrices, String text, float x, float y, int color)
    {
        if (text.isEmpty())
        {
            return;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            FontManager.FONT.drawStringWithShadow(matrices, text, x, y, color);
            return;
        }

        MinecraftClient.getInstance().textRenderer.draw(
                text,
                x,
                y,
                color,
                true,
                matrices.peek().getPositionMatrix(),
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE);

        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
    }

    public float getTextWidth(String text)
    {
        if (text.isEmpty())
        {
            return 0;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            return FontManager.FONT.getStringWidth(text);
        }

        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    private static void startRender()
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.disableDepthTest();
    }

    private static void endRender()
    {
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
}

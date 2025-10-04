package net.shoreline.client.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.imixin.IWorldRenderer;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
public class RenderManager
{
    private final List<BatchedBoxRender> quadQueue = new ObjectArrayList<>(512);
    private final List<BatchedBoxRender> lineQuadQueue = new ObjectArrayList<>(512);
    private final List<BatchedLineRender> lineQueue = new ObjectArrayList<>(1024);

    private double deltaTime;

    public RenderManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        flushBuffer();
    }

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
        if (isVisible(box))
        {
            queueQuad(matrixStack, box, color, false);
        }
    }

    public void renderBoundingBox(MatrixStack matrixStack, BlockPos pos, int color)
    {
        renderBoundingBox(matrixStack, new Box(pos), color);
    }

    public void renderBoundingBox(MatrixStack matrixStack, Box box, int color)
    {
        if (isVisible(box))
        {
            queueLineQuad(matrixStack, box, color, false);
        }
    }

    public void renderLine(MatrixStack matrices, Vec3d start, Vec3d end, int color)
    {
        double minX = Math.min(start.x, end.x);
        double minY = Math.min(start.y, end.y);
        double minZ = Math.min(start.z, end.z);
        double maxX = Math.max(start.x, end.x);
        double maxY = Math.max(start.y, end.y);
        double maxZ = Math.max(start.z, end.z);
        Box bounds = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        if (isVisible(bounds))
        {
            queueLine(matrices, start, end, color, false);
        }
    }

    public void renderBox(Consumer<BufferBuilder> consumer, boolean depth)
    {
        startRender(depth);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        consumer.accept(buffer);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public void renderBoundingBox(Consumer<BufferBuilder> consumer, boolean depth)
    {
        startRender(depth);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        consumer.accept(buffer);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    private void queueQuad(MatrixStack matrices, Box box, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        quadQueue.add(new BatchedBoxRender(m, color, depth, box));
    }

    private void queueLineQuad(MatrixStack matrices, Box box, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        lineQuadQueue.add(new BatchedBoxRender(m, color, depth, box));
    }

    private void queueLine(MatrixStack matrices, Vec3d start, Vec3d end, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        lineQueue.add(new BatchedLineRender(m, color, depth, start, end));
    }

    private void queueLinePoint(MatrixStack matrices, Vec3d point, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        lineQueue.add(new BatchedLineRender(m, color, depth, point, null));
    }

    public void flushBuffer()
    {
        flushQuads();
        flushLinesQuad();
        flushLines();

        quadQueue.clear();
        lineQuadQueue.clear();
        lineQueue.clear();
    }

    private void flushQuads()
    {
        if (quadQueue.isEmpty())
        {
            return;
        }

        startRender(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();

        for (BatchedBoxRender batchedRender : quadQueue)
        {
            float minX = (float) (batchedRender.box.minX - camera.x);
            float minY = (float) (batchedRender.box.minY - camera.y);
            float minZ = (float) (batchedRender.box.minZ - camera.z);
            float maxX = (float) (batchedRender.box.maxX - camera.x);
            float maxY = (float) (batchedRender.box.maxY - camera.y);
            float maxZ = (float) (batchedRender.box.maxZ - camera.z);

            buffer.vertex(batchedRender.matrix, minX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, minZ).color(batchedRender.color);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    private void flushLinesQuad()
    {
        if (lineQuadQueue.isEmpty())
        {
            return;
        }

        startRender(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();

        for (BatchedBoxRender batchedRender : lineQuadQueue)
        {
            float minX = (float) (batchedRender.box.minX - camera.x);
            float minY = (float) (batchedRender.box.minY - camera.y);
            float minZ = (float) (batchedRender.box.minZ - camera.z);
            float maxX = (float) (batchedRender.box.maxX - camera.x);
            float maxY = (float) (batchedRender.box.maxY - camera.y);
            float maxZ = (float) (batchedRender.box.maxZ - camera.z);

            buffer.vertex(batchedRender.matrix, minX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, minZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, maxX, maxY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, minY, maxZ).color(batchedRender.color);
            buffer.vertex(batchedRender.matrix, minX, maxY, maxZ).color(batchedRender.color);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    private void flushLines()
    {
        if (lineQueue.isEmpty())
        {
            return;
        }

        startRender(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Vec3d camera = MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos();

        for (BatchedLineRender batchedRender : lineQueue)
        {
            float x1 = (float) (batchedRender.start.x - camera.x);
            float y1 = (float) (batchedRender.start.y - camera.y);
            float z1 = (float) (batchedRender.start.z - camera.z);
            buffer.vertex(batchedRender.matrix, x1, y1, z1).color(batchedRender.color);

            if (batchedRender.end != null)
            {
                float x2 = (float) (batchedRender.end.x - camera.x);
                float y2 = (float) (batchedRender.end.y - camera.y);
                float z2 = (float) (batchedRender.end.z - camera.z);
                buffer.vertex(batchedRender.matrix, x2, y2, z2).color(batchedRender.color);
            }
        }

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

    public void drawRect(DrawContext context, float x, float y, float width, float height, int color)
    {
        float i;
        float x2 = x + width;
        float y2 = y + height;
        if (x < x2)
        {
            i = x;
            x = x2;
            x2 = i;
        }

        if (y < y2)
        {
            i = y;
            y = y2;
            y2 = i;
        }

        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vc = ((IDrawContext) context).getVertexConsumerProvider().getBuffer(RenderLayer.getGui());
        vc.vertex(matrix4f, x, y, 0).color(color);
        vc.vertex(matrix4f, x, y2, 0).color(color);
        vc.vertex(matrix4f, x2, y2, 0).color(color);
        vc.vertex(matrix4f, x2, y, 0).color(color);
    }

    public void drawOutline(DrawContext context, float x, float y, float width, float height, float thickness, int color)
    {
        float t2 = thickness * 2;
        drawRect(context, x - thickness, y - thickness, width + t2, thickness, color);
        drawRect(context, x - thickness, y, thickness, height, color);
        drawRect(context, x + width, y, thickness, height, color);
        drawRect(context, x - thickness, y + height, width + t2, thickness, color);
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

    private static void startRender(boolean depth)
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        if (depth)
        {
            RenderSystem.enableDepthTest();
        }
        else
        {
            RenderSystem.disableDepthTest();
        }
    }

    private static void endRender()
    {
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public boolean isVisible(Box box)
    {
        return ((IWorldRenderer) MinecraftClient.getInstance().worldRenderer)
                .getFrustum().isVisible(box);
    }

    @RequiredArgsConstructor
    private static class BatchedRender
    {
        public final Matrix4f matrix;
        public final int color;
        public final boolean depth;
    }

    private static class BatchedBoxRender extends BatchedRender
    {
        public final Box box;

        public BatchedBoxRender(Matrix4f matrix, int color, boolean depth, Box box)
        {
            super(matrix, color, depth);
            this.box = box;
        }
    }

    private static class BatchedLineRender extends BatchedRender
    {
        public final Vec3d start;
        public final Vec3d end;

        public BatchedLineRender(Matrix4f matrix, int color, boolean depth, Vec3d start, @Nullable Vec3d end)
        {
            super(matrix, color, depth);
            this.start = start;
            this.end = end;
        }
    }
}

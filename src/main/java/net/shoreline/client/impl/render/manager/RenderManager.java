package net.shoreline.client.impl.render.manager;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.imixin.IWorldRenderer;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.client.impl.render.Mesh;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
public class RenderManager extends GenericFeature
{
    private final List<Mesh> meshQueue = new ArrayList<>(1024);

    private final List<BoxRender> quadQueue = new ObjectArrayList<>(512);
    private final List<BoxRender> lineQuadQueue = new ObjectArrayList<>(512);
    private final List<LineRender> lineQueue = new ObjectArrayList<>(1024);
    private final List<TextRender> textQueue = new ObjectArrayList<>(512);

    private double deltaTime;

    public RenderManager()
    {
        super("Custom Rendering");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        flushQuadsBuffer();
        flushLinesQuadBuffer();

        flushLinesBuffer(event.getMatrixStack());

        flushTextBuffer();
        flushMeshes();

        quadQueue.clear();
        lineQuadQueue.clear();
        lineQueue.clear();

        meshQueue.clear();
        textQueue.clear();
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

    public void renderNametag(MatrixStack matrixStack, Vec3d pos, float scale, String text, int color)
    {
        if (isVisible(Box.from(pos)))
        {
            queueText(matrixStack, pos, text, scale, color, false);
        }
    }

    public void queueMesh(Mesh mesh)
    {
        meshQueue.add(mesh);
    }

    private void queueQuad(MatrixStack matrices, Box box, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        quadQueue.add(new BoxRender(m, color, depth, box));
    }

    private void queueLineQuad(MatrixStack matrices, Box box, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        lineQuadQueue.add(new BoxRender(m, color, depth, box));
    }

    private void queueLine(MatrixStack matrices, Vec3d start, Vec3d end, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        lineQueue.add(new LineRender(m, color, depth, start, end));
    }

    private void queueLinePoint(MatrixStack matrices, Vec3d point, int color, boolean depth)
    {
        Matrix4f m = new Matrix4f(matrices.peek().getPositionMatrix());
        lineQueue.add(new LineRender(m, color, depth, point, null));
    }

    private void queueText(MatrixStack matrices, Vec3d start, String text, float scale, int color, boolean depth)
    {
        textQueue.add(new TextRender(matrices, color, depth, start, scale, text));
    }

    public void renderBox(Consumer<BufferBuilder> buffer)
    {
        startRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.accept(builder);
        BuiltBuffer built = builder.endNullable();
        if (built != null)
        {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        endRender();
    }

    public void renderBoundingBox(Consumer<BufferBuilder> buffer)
    {
        startRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.accept(builder);
        BuiltBuffer built = builder.endNullable();
        if (built != null)
        {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        endRender();
    }

    private void flushMeshes()
    {
        if (meshQueue.isEmpty())
        {
            return;
        }

        for (Mesh mesh : meshQueue)
        {
            mesh.flushVertices(mc.getBufferBuilders().getEntityVertexConsumers());
        }
    }

    private void flushQuadsBuffer()
    {
        if (quadQueue.isEmpty())
        {
            return;
        }

        startRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vec3d camera = mc.getEntityRenderDispatcher().camera.getPos();

        for (BoxRender batchedRender : quadQueue)
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

    private void flushLinesQuadBuffer()
    {
        if (lineQuadQueue.isEmpty())
        {
            return;
        }

        startRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Vec3d camera = mc.getEntityRenderDispatcher().camera.getPos();

        for (BoxRender batchedRender : lineQuadQueue)
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

    private void flushLinesBuffer(MatrixStack matrices)
    {
        if (lineQueue.isEmpty())
        {
            return;
        }

        startRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.backupProjectionMatrix();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Camera camera = mc.getEntityRenderDispatcher().camera;
        float cx = (float) camera.getPos().x;
        float cy = (float) camera.getPos().y;
        float cz = (float) camera.getPos().z;

        MatrixStack matrixStack = new MatrixStack();
        float h = mc.options.getFov().getValue();
        Matrix4f matrix4f = mc.gameRenderer.getBasicProjectionMatrix(h);
        matrix4f.mul(matrixStack.peek().getPositionMatrix());

        for (int i = 0; i < lineQueue.size(); i++)
        {
            RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.PERSPECTIVE);
            LineRender batchedRender = lineQueue.get(i);
            float x1 = (float) batchedRender.start.x - cx;
            float y1 = (float) batchedRender.start.y - cy;
            float z1 = (float) batchedRender.start.z - cz;

            float x2;
            float y2;
            float z2;
            if (batchedRender.end != null)
            {
                x2 = (float) batchedRender.end.x - cx;
                y2 = (float) batchedRender.end.y - cy;
                z2 = (float) batchedRender.end.z - cz;
            } else
            {
                if (i > 0)
                {
                    LineRender prevRender = lineQueue.get(i - 1);
                    x2 = (float) prevRender.end.x - cx;
                    y2 = (float) prevRender.end.y - cy;
                    z2 = (float) prevRender.end.z - cz;;
                } else
                {
                    x2 = cx;
                    y2 = cy;
                    z2 = cz;
                }
            }

            float k = x2 - x1;
            float l = y2 - y1;
            float m = z2 - z1;
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;

            Matrix3f normal = matrices.peek().getNormalMatrix();
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            Vector3f vector3f = normal.transform(k, l, m, new Vector3f()).normalize();
            Vector4f vector4f = positionMatrix.transform(new Vector4f(x1, y1, z1, 1.0f));

            buffer.vertex(vector4f.x, vector4f.y, vector4f.z)
                    .normal(vector3f.x, vector3f.y, vector3f.z)
                    .color(batchedRender.color);

            if (batchedRender.end != null)
            {
                Vector4f vector4f2 = positionMatrix.transform(new Vector4f(x2, y2, z2, 1.0f));
                buffer.vertex(vector4f2.x, vector4f2.y, vector4f2.z)
                        .normal(vector3f.x, vector3f.y, vector3f.z)
                        .color(batchedRender.color);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.restoreProjectionMatrix();
        endRender();
    }

    private void flushTextBuffer()
    {
        if (textQueue.isEmpty())
        {
            return;
        }
        
        EntityRenderDispatcher entityRenderer = mc.getEntityRenderDispatcher();
        Camera camera = entityRenderer.camera;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(1.0f, -32500000);

        for (TextRender render : textQueue) 
        {
            float distance = (float) Math.sqrt(camera.getPos().squaredDistanceTo(render.pos));
            float scaling = 0.0018f + render.scale * distance;
            if (distance <= 8.0)
            {
                scaling = 0.0245f;
            }

            Vec3d pos = render.pos.subtract(camera.getPos());
            MatrixStack matrixStack = render.matrixStack;
            matrixStack.push();
            matrixStack.translate(pos);
            matrixStack.multiply(entityRenderer.getRotation());
            matrixStack.scale(scaling, -scaling, scaling);

            float hwidth = getTextWidth(render.text) / 2.0f;
            drawText(matrixStack, render.text, (int) -hwidth, 0, render.color);

            matrixStack.pop();
        }
        
        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(1.0f, 32500000);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public void drawRect(MatrixStack matrixStack, float x, float y, float width, float height, int color)
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

        RenderSystem.enableBlend();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(color);
        bufferBuilder.vertex(matrix4f, x, y2, 0).color(color);
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(color);
        bufferBuilder.vertex(matrix4f, x2, y, 0).color(color);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
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
            FontManager.FONT_RENDERER.drawStringWithShadow(matrices, text, x, y, color);
            return;
        }

        mc.textRenderer.draw(
                text,
                x,
                y,
                color,
                true,
                matrices.peek().getPositionMatrix(),
                mc.getBufferBuilders().getEntityVertexConsumers(),
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE);

        mc.getBufferBuilders().getEntityVertexConsumers().draw();
    }

    public float getTextWidth(String text)
    {
        if (text.isEmpty())
        {
            return 0;
        }

        if (FontModule.INSTANCE.isEnabled())
        {
            return FontManager.FONT_RENDERER.getStringWidth(text);
        }

        return mc.textRenderer.getWidth(text);
    }

    private void startRender()
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.disableDepthTest();
    }

    private void endRender()
    {
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public boolean isVisible(Box box)
    {
        return ((IWorldRenderer) mc.worldRenderer).getFrustum().isVisible(box);
    }

    @RequiredArgsConstructor
    private class BatchedRender
    {
        public final Matrix4f matrix;
        public final int color;
        public final boolean depth;
    }

    private class BoxRender extends BatchedRender
    {
        public final Box box;

        public BoxRender(Matrix4f matrix,
                         int color,
                         boolean depth,
                         Box box)
        {
            super(matrix, color, depth);
            this.box = box;
        }
    }

    private class LineRender extends BatchedRender
    {
        public final Vec3d start;
        public final Vec3d end;

        public LineRender(Matrix4f matrix,
                          int color,
                          boolean depth,
                          Vec3d start,
                          @Nullable Vec3d end)
        {
            super(matrix, color, depth);
            this.start = start;
            this.end = end;
        }
    }

    private class TextRender extends BatchedRender
    {
        private final MatrixStack matrixStack;
        private final Vec3d pos;
        private final float scale;
        private final String text;

        public TextRender(MatrixStack matrixStack,
                          int color,
                          boolean depth,
                          Vec3d pos,
                          float scale,
                          String text)
        {
            super(new Matrix4f(matrixStack.peek().getPositionMatrix()), color, depth);
            this.matrixStack = matrixStack;
            this.pos = pos;
            this.scale = scale;
            this.text = text;
        }
    }
}

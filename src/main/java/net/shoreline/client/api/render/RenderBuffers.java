package net.shoreline.client.api.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Calling these outside of {@link net.shoreline.client.impl.event.render.RenderWorldEvent} will blow everything up
 */
public class RenderBuffers
{
    public static final Buffer QUADS = new Buffer(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    public static final Buffer TEXTURE_QUADS = new Buffer(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
    public static final Buffer LINES = new Buffer(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
    private static final List<Runnable> postRenderCallbacks = new ArrayList<>();
    private static boolean isSetup = false;

    public static void preRender()
    {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        isSetup = true;
    }

    public static void postRender()
    {
        QUADS.draw();
        LINES.draw();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        isSetup = false;

        for (Runnable callback : postRenderCallbacks)
        {
            callback.run();
        }
        postRenderCallbacks.clear();
    }

    public static void post(Runnable callback)
    {
        if (isSetup)
        {
            postRenderCallbacks.add(callback);
        }
        else
        {
            callback.run();
        }
    }

    private static Matrix4d toMatrix4d(Matrix4f matrix4f)
    {
        return new Matrix4d(matrix4f.m00(), matrix4f.m01(), matrix4f.m02(), matrix4f.m03(),
                matrix4f.m10(), matrix4f.m11(), matrix4f.m12(), matrix4f.m13(),
                matrix4f.m20(), matrix4f.m21(), matrix4f.m22(), matrix4f.m23(),
                matrix4f.m30(), matrix4f.m31(), matrix4f.m32(), matrix4f.m33());
    }

    public static class Buffer
    {
        public final BufferBuilder buffer = new BufferBuilder(2048);
        private final VertexFormat.DrawMode drawMode;
        private final VertexFormat vertexFormat;
        private Matrix4d positionMatrix;
        private Matrix3f normalMatrix;

        public Buffer(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat)
        {
            this.drawMode = drawMode;
            this.vertexFormat = vertexFormat;
        }

        public void begin(MatrixStack stack)
        {
            this.positionMatrix = toMatrix4d(stack.peek().getPositionMatrix());
            this.normalMatrix = stack.peek().getNormalMatrix();
            Vec3d pos = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera.getPos();
            positionMatrix.translate(-pos.x, -pos.y, -pos.z);
            if (!buffer.isBuilding()) buffer.begin(drawMode, vertexFormat);
        }

        /**
         * render in immediate mode if we're calling from outside of {@link net.shoreline.client.impl.event.render.RenderWorldEvent}
         */
        public void end()
        {
            if (!isSetup) draw();
        }

        public Buffer vertex(double x, double y, double z)
        {
            Vector4d vector4d = positionMatrix.transform(new Vector4d(x, y, z, 1.0));
            this.buffer.vertex(vector4d.x(), vector4d.y(), vector4d.z()).next();
            return this;
        }

        public Buffer vertexTex(double x, double y, double z, float u, float v)
        {
            Vector4d vector4d = positionMatrix.transform(new Vector4d(x, y, z, 1.0));
            this.buffer.vertex(vector4d.x(), vector4d.y(), vector4d.z()).texture(u, v).next();
            return this;
        }

        public Buffer vertexLine(double x1, double y1, double z1, double x2, double y2, double z2)
        {
            float k = (float)(x2 - x1);
            float l = (float)(y2 - y1);
            float m = (float)(z2 - z1);
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;
            Vector4d vector4d = positionMatrix.transform(new Vector4d(x1, y1, z1, 1.0));
            this.buffer.vertex(vector4d.x(), vector4d.y(), vector4d.z()).normal(normalMatrix, k, l, m).next();
            Vector4d vector4d2 = positionMatrix.transform(new Vector4d(x2, y2, z2, 1.0));
            this.buffer.vertex(vector4d2.x(), vector4d2.y(), vector4d2.z()).normal(normalMatrix, k, l, m).next();
            return this;
        }

        public void color(int color)
        {
            this.buffer.unfixColor();
            this.buffer.fixedColor(
                    ColorHelper.Argb.getRed(color),
                    ColorHelper.Argb.getGreen(color),
                    ColorHelper.Argb.getBlue(color),
                    ColorHelper.Argb.getAlpha(color)
            );
        }

        public void draw()
        {
            if (this.buffer.isBuilding())
            {
                if (this.buffer.isBatchEmpty())
                {
                    this.buffer.clear();
                }
                else
                {
                    if (vertexFormat == VertexFormats.LINES)
                    {
                        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
                    }
                    else if (vertexFormat == VertexFormats.POSITION_COLOR)
                    {
                        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    }
                    else if (vertexFormat == VertexFormats.POSITION_TEXTURE)
                    {
                        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                    }

                    BufferRenderer.drawWithGlobalProgram(this.buffer.end());
                }
            }
        }
    }
}
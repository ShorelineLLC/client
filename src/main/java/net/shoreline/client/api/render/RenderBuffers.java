package net.shoreline.client.api.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Calling these outside of {@link net.shoreline.client.impl.event.render.RenderWorldEvent} will blow everything up
 */
public class RenderBuffers
{
    public static final Buffer QUADS = new Buffer(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    public static final Buffer LINES = new Buffer(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
    private static final List<Runnable> postRenderCallbacks = new ArrayList<>();
    private static boolean isSetup = false;

    public static void preRender()
    {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableCull();
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
        RenderSystem.disableCull();
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

    public static class Buffer
    {
        public final BufferBuilder buffer = new BufferBuilder(2048);
        private final VertexFormat.DrawMode drawMode;
        private final VertexFormat vertexFormat;
        private Matrix4f positionMatrix;

        public Buffer(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat)
        {
            this.drawMode = drawMode;
            this.vertexFormat = vertexFormat;
        }

        public void begin(Matrix4f positionMatrix)
        {
            this.positionMatrix = positionMatrix;
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
            return vertex((float) x, (float) y, (float) z);
        }

        public Buffer vertex(float x, float y, float z)
        {
            this.buffer.vertex(positionMatrix, x, y, z).next();
            return this;
        }

        public void color(int color)
        {
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
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    BufferRenderer.drawWithGlobalProgram(this.buffer.end());
                }
            }
        }
    }
}
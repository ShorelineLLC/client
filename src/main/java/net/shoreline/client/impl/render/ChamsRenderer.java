package net.shoreline.client.impl.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.EndCrystalEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.imixin.IEndCrystalEntityRenderer;
import net.shoreline.client.impl.imixin.ILivingEntityRenderer;
import net.shoreline.client.impl.imixin.IMultiPhase;
import net.shoreline.client.impl.imixin.IMultiPhaseParameters;
import org.joml.Matrix4f;

public enum ChamsRenderer
{
    NONE,
    CHAMS,
    WIREFRAME,
    BOTH;

    private static ChamsRenderer chams;
    private static Matrix4f matrix;
    private static Vec3d position;
    private static int color;
    private static boolean throughWalls;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void render(ChamsRenderer chams, Entity entity, float tickDelta, boolean throughWalls, int color)
    {
        if (chams == NONE)
        {
            return;
        }

        MatrixStack matrices = new MatrixStack();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        ChamsRenderer.chams = chams;
        ChamsRenderer.color = color;
        ChamsRenderer.matrix = matrix4f;
        ChamsRenderer.throughWalls = throughWalls;
        ChamsRenderer.position = Interpolation.getRenderPosition(entity, tickDelta);

        EntityRenderer<?, ?> renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState renderState = ((EntityRenderer<Entity, EntityRenderState>) renderer).getAndUpdateRenderState(entity, tickDelta);
        matrices.push();
        if (renderer instanceof LivingEntityRenderer livingEntityRenderer && renderState instanceof LivingEntityRenderState state)
        {
            ((ILivingEntityRenderer) livingEntityRenderer).skipShineRendering(true);
            livingEntityRenderer.render(state, matrices, CustomVertexConsumerProvider.INSTANCE, 15);
            ((ILivingEntityRenderer) livingEntityRenderer).skipShineRendering(false);
        }

        if (renderer instanceof EndCrystalEntityRenderer crystalRenderer && renderState instanceof EndCrystalEntityRenderState state)
        {
            ((IEndCrystalEntityRenderer) crystalRenderer).skipShineRendering(true);
            crystalRenderer.render(state, matrices, CustomVertexConsumerProvider.INSTANCE, 15);
            ((IEndCrystalEntityRenderer) crystalRenderer).skipShineRendering(false);
        }

        matrices.pop();
    }

    private static class CustomVertexConsumerProvider implements VertexConsumerProvider
    {
        public static final CustomVertexConsumerProvider INSTANCE = new CustomVertexConsumerProvider();

        @Override
        public VertexConsumer getBuffer(RenderLayer layer)
        {
            if (layer instanceof IMultiPhase phase && ((IMultiPhaseParameters) (Object) phase.hookGetPhases()).getTarget() == RenderLayer.ITEM_ENTITY_TARGET)
            {
                return EmptyVertexConsumer.INSTANCE;
            }

            return CustomVertexConsumer.INSTANCE;
        }
    }

    private static class CustomVertexConsumer implements VertexConsumer
    {
        public static final CustomVertexConsumer INSTANCE = new CustomVertexConsumer();
        private final float[] xs = new float[4];
        private final float[] ys = new float[4];
        private final float[] zs = new float[4];
        private int i = 0;

        @Override
        public VertexConsumer vertex(float x, float y, float z)
        {
            xs[i] = x;
            ys[i] = y;
            zs[i] = z;
            i++;

            if (i == 4)
            {
                Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
                if ((chams == CHAMS || chams == BOTH))
                {
                    Mesh mesh = new Mesh(Layers.QUADS, matrix);
                    mesh.vertex(position.getX() + xs[0] - camera.getX(), position.getY() + ys[0] - camera.getY(), position.getZ() + zs[0] - camera.getZ(), color);
                    mesh.vertex(position.getX() + xs[1] - camera.getX(), position.getY() + ys[1] - camera.getY(), position.getZ() + zs[1] - camera.getZ(), color);
                    mesh.vertex(position.getX() + xs[2] - camera.getX(), position.getY() + ys[2] - camera.getY(), position.getZ() + zs[2] - camera.getZ(), color);
                    mesh.vertex(position.getX() + xs[3] - camera.getX(), position.getY() + ys[3] - camera.getY(), position.getZ() + zs[3] - camera.getZ(), color);
                    mesh.flushVertices(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
                }

                if ((chams == WIREFRAME || chams == BOTH))
                {
                    int lineColor = ColorUtil.withTransparency(color, 0.75f);
                    Mesh mesh = new Mesh(RenderLayer.getDebugLineStrip(1.5f), matrix);
                    mesh.vertex(position.x + xs[0] - camera.getX(), position.y + ys[0] - camera.getY(), position.z + zs[0] - camera.getZ(), lineColor);
                    mesh.vertex(position.x + xs[1] - camera.getX(), position.y + ys[1] - camera.getY(), position.z + zs[1] - camera.getZ(), lineColor);
                    mesh.vertex(position.x + xs[1] - camera.getX(), position.y + ys[1] - camera.getY(), position.z + zs[1] - camera.getZ(), lineColor);
                    mesh.vertex(position.x + xs[2] - camera.getX(), position.y + ys[2] - camera.getY(), position.z + zs[2] - camera.getZ(), lineColor);
                    mesh.vertex(position.x + xs[2] - camera.getX(), position.y + ys[2] - camera.getY(), position.z + zs[2] - camera.getZ(), lineColor);
                    mesh.vertex(position.x + xs[3] - camera.getX(), position.y + ys[3] - camera.getY(), position.z + zs[3] - camera.getZ(), lineColor);
                    mesh.flushVertices(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
                }

                i = 0;
            }

            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha)
        {
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v)
        {
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v)
        {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v)
        {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z)
        {
            return this;
        }
    }

    private static class EmptyVertexConsumer implements VertexConsumer
    {
        private static final EmptyVertexConsumer INSTANCE = new EmptyVertexConsumer();

        @Override
        public VertexConsumer vertex(float x, float y, float z)
        {
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha)
        {
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v)
        {
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v)
        {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v)
        {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z)
        {
            return this;
        }
    }
}

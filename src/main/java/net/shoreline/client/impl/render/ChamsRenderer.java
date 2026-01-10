package net.shoreline.client.impl.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.imixin.*;
import net.shoreline.client.impl.module.render.ChamsModule;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@SuppressWarnings("unchecked")
public enum ChamsRenderer
{
    NONE,
    CHAMS,
    WIREFRAME,
    BOTH;

    private static final MatrixStack matrices = new MatrixStack();
    private static final Matrix4f matrix = matrices.peek().getPositionMatrix();

    public static boolean rendering = false;
    private static ChamsRenderer chams;
    private static Vec3d position;
    private static int color;
    private static float factor;

    public static void render(ChamsRenderer chams, Entity entity, float tickDelta, int color)
    {
        render(chams, entity, tickDelta, color, 1.0f);
    }

    public static void render(ChamsRenderer chams, Entity entity, float tickDelta, int color, float factor)
    {
        if (chams == NONE)
        {
            return;
        }

        ChamsRenderer.chams = chams;
        ChamsRenderer.color = color;
        ChamsRenderer.position = Interpolation.getRenderPosition(entity, tickDelta);
        ChamsRenderer.factor = factor;

        matrices.push();
        rendering = true;
        var renderer = (EntityRenderer<Entity, EntityRenderState>) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        var renderState = renderer.getAndUpdateRenderState(entity, tickDelta);

        if (renderer instanceof LivingEntityRenderer<?,?,?> livingEntityRenderer)
        {
            Model model = livingEntityRenderer.getModel();
            if (model instanceof PlayerEntityModel playerEntityModel)
            {
                boolean extraLayer = ChamsModule.getInstance().extraLayer.getValue();
                playerEntityModel.leftPants.visible = extraLayer;
                playerEntityModel.rightPants.visible = extraLayer;
                playerEntityModel.leftSleeve.visible = extraLayer;
                playerEntityModel.rightSleeve.visible = extraLayer;
                playerEntityModel.jacket.visible = extraLayer;
                playerEntityModel.hat.visible = extraLayer;
            }
        }

        renderer.render(renderState, matrices, CustomVertexConsumerProvider.INSTANCE, 15);
        rendering = false;
        matrices.pop();
    }

    public static class CustomVertexConsumerProvider implements VertexConsumerProvider
    {
        public static final CustomVertexConsumerProvider INSTANCE = new CustomVertexConsumerProvider();

        @Override
        public VertexConsumer getBuffer(RenderLayer layer)
        {
            if (layer instanceof IMultiPhase phase && ((IMultiPhaseParameters) (Object) phase.hookGetPhases()).getTarget() == RenderPhase.ITEM_ENTITY_TARGET)
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
                    Managers.RENDER.renderBox(buffer ->
                    {
                        int boxColor = ColorUtil.withTransparency(color, factor);
                        buffer.vertex(matrix, (float) (position.getX() + xs[0] - camera.getX()), (float) (position.getY() + ys[0] - camera.getY()), (float) (position.getZ() + zs[0] - camera.getZ())).color(boxColor);
                        buffer.vertex(matrix, (float) (position.getX() + xs[1] - camera.getX()), (float) (position.getY() + ys[1] - camera.getY()), (float) (position.getZ() + zs[1] - camera.getZ())).color(boxColor);
                        buffer.vertex(matrix, (float) (position.getX() + xs[2] - camera.getX()), (float) (position.getY() + ys[2] - camera.getY()), (float) (position.getZ() + zs[2] - camera.getZ())).color(boxColor);
                        buffer.vertex(matrix, (float) (position.getX() + xs[3] - camera.getX()), (float) (position.getY() + ys[3] - camera.getY()), (float) (position.getZ() + zs[3] - camera.getZ())).color(boxColor);
                    });
                }

                if ((chams == WIREFRAME || chams == BOTH))
                {
                    Managers.RENDER.renderBoundingBox(buffer ->
                    {
                        int lineColor = (color & 0x00FFFFFF) | 0xFF000000;
                        lineColor = ColorUtil.withTransparency(lineColor, factor);
                        buffer.vertex(matrix, (float) (position.x + xs[0] - camera.getX()), (float) (position.y + ys[0] - camera.getY()), (float) (position.z + zs[0] - camera.getZ())).color(lineColor);
                        buffer.vertex(matrix, (float) (position.x + xs[1] - camera.getX()), (float) (position.y + ys[1] - camera.getY()), (float) (position.z + zs[1] - camera.getZ())).color(lineColor);
                        buffer.vertex(matrix, (float) (position.x + xs[1] - camera.getX()), (float) (position.y + ys[1] - camera.getY()), (float) (position.z + zs[1] - camera.getZ())).color(lineColor);
                        buffer.vertex(matrix, (float) (position.x + xs[2] - camera.getX()), (float) (position.y + ys[2] - camera.getY()), (float) (position.z + zs[2] - camera.getZ())).color(lineColor);
                        buffer.vertex(matrix, (float) (position.x + xs[2] - camera.getX()), (float) (position.y + ys[2] - camera.getY()), (float) (position.z + zs[2] - camera.getZ())).color(lineColor);
                        buffer.vertex(matrix, (float) (position.x + xs[3] - camera.getX()), (float) (position.y + ys[3] - camera.getY()), (float) (position.z + zs[3] - camera.getZ())).color(lineColor);
                    });
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

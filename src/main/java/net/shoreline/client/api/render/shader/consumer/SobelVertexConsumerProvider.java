package net.shoreline.client.api.render.shader.consumer;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public class SobelVertexConsumerProvider implements VertexConsumerProvider
{
    private final VertexConsumer vertexConsumer;

    public SobelVertexConsumerProvider()
    {
        this.vertexConsumer = new SobelVertexConsumer();
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer)
    {
        return vertexConsumer;
    }

    public static class SobelVertexConsumer implements VertexConsumer
    {

        @Override
        public VertexConsumer vertex(float x, float y, float z)
        {
            return null;
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
            return null;
        }
    }
}

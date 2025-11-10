package net.shoreline.client.impl.render.util;


import net.minecraft.client.render.VertexConsumer;

public class ClientDual implements VertexConsumer
{
    private final VertexConsumer first;
    private final VertexConsumer second;

    public ClientDual(VertexConsumer first, VertexConsumer second)
    {
        this.first = first;
        this.second = second;
    }

    public VertexConsumer vertex(float x, float y, float z) {
        this.first.vertex(x, y, z);
        this.second.vertex(x, y, z);
        return this;
    }

    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.first.color(red, green, blue, alpha);
        this.second.color(red, green, blue, alpha);
        return this;
    }

    public VertexConsumer texture(float u, float v) {
        this.first.texture(u, v);
        this.second.texture(u, v);
        return this;
    }

    public VertexConsumer overlay(int u, int v) {
        this.first.overlay(u, v);
        this.second.overlay(u, v);
        return this;
    }

    public VertexConsumer light(int u, int v) {
        this.first.light(u, v);
        this.second.light(u, v);
        return this;
    }

    public VertexConsumer normal(float x, float y, float z) {
        this.first.normal(x, y, z);
        this.second.normal(x, y, z);
        return this;
    }

    public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ)
    {
        this.first.vertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
        this.second.vertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
    }
}
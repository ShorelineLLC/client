package net.shoreline.client.impl.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Mesh
{
    private final RenderLayer layer;
    private final Matrix4f matrix;
    private List<Vertex> vertices;

    public Mesh(RenderLayer renderLayer, Matrix4f matrix)
    {
        this(renderLayer, matrix, new ArrayList<>());
    }

    public Mesh(RenderLayer renderLayer, Matrix4f matrix, List<Vertex> vertices)
    {
        this.layer = renderLayer;
        this.matrix = matrix;
        this.vertices = vertices;
    }

    public void flushVertices(VertexConsumerProvider provider)
    {
        VertexConsumer consumer = provider.getBuffer(layer);
        for (Vertex vertex : vertices)
        {
            consumer.vertex(matrix, vertex.getX(), vertex.getY(), vertex.getZ()).color(vertex.getColor());
        }
    }

    public void vertex(double x, double y, double z, int color)
    {
        vertices.add(new Vertex((float) x, (float) y, (float) z, color));
    }

    public void vertex(float x, float y, float z, int color)
    {
        vertices.add(new Vertex(x, y, z, color));
    }
}

package net.shoreline.client.impl.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Mesh
{
    private final RenderLayer layer;
    private final MatrixStack matrices;
    private List<Vertex> vertices;

    public Mesh(RenderLayer renderLayer)
    {
        this(renderLayer, null);
    }

    public Mesh(RenderLayer renderLayer, MatrixStack matrices)
    {
        this(renderLayer, matrices, new ArrayList<>());
    }

    public Mesh(RenderLayer renderLayer, MatrixStack matrices, List<Vertex> vertices)
    {
        this.layer = renderLayer;
        this.matrices = matrices;
        this.vertices = vertices;
    }

    public void flushVertices(VertexConsumerProvider.Immediate provider)
    {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = provider.getBuffer(layer);
        for (Vertex vertex : vertices)
        {
            consumer.vertex(matrix, vertex.getX(), vertex.getY(), vertex.getZ()).color(vertex.getColor());
        }
    }

    public BuiltBuffer getBuiltBuffer()
    {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().begin(layer.getDrawMode(), layer.getVertexFormat());
        for (Vertex vertex : vertices)
        {
            builder.vertex(matrix, vertex.getX(), vertex.getY(), vertex.getZ()).color(vertex.getColor());
        }

        return builder.end();
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

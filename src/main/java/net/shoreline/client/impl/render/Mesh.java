package net.shoreline.client.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.imixin.IGameRenderer;
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

    public Mesh(RenderLayer renderLayer)
    {
        this(renderLayer, null);
    }

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

    public void flushVertices(VertexConsumerProvider.Immediate provider)
    {
        VertexConsumer consumer = provider.getBuffer(layer);
        for (Vertex vertex : vertices)
        {
            consumer.vertex(matrix, vertex.getX(), vertex.getY(), vertex.getZ()).color(vertex.getColor());
        }
    }

    public BuiltBuffer getBuiltBuffer()
    {
        BufferBuilder builder = Tessellator.getInstance().begin(layer.getDrawMode(), layer.getVertexFormat());
        for (Vertex vertex : vertices)
        {
            builder.vertex(vertex.getX(), vertex.getY(), vertex.getZ()).color(vertex.getColor());
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

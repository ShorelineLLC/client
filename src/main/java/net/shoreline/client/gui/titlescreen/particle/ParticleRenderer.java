package net.shoreline.client.gui.titlescreen.particle;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.imixin.IDrawContext;
import net.shoreline.client.impl.render.manager.ShaderManager;

import java.util.List;

public class ParticleRenderer<T extends Particle>
{
    private final RenderLayer layer;

    public ParticleRenderer(RenderLayer layer)
    {
        this.layer = layer;
    }

    public void render(DrawContext context, List<T> particles)
    {
        VertexConsumerProvider provider = ((IDrawContext) context).getVertexConsumerProvider();
        VertexConsumer consumer = provider.getBuffer(layer);

        for (T particle : particles)
        {
            if (particle.isWindingUp())
            {
                continue;
            }

            particle.render(consumer);
        }
    }
}
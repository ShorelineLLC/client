package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.VertexConsumerProvider;

@IMixin
public interface IDrawContext
{
    VertexConsumerProvider.Immediate getVertexConsumerProvider();
}

package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

@IMixin
public interface ILivingEntityRenderer
{
    void renderChams(LivingEntityRenderState livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i);
}

package net.shoreline.client.mixin.render.block.entity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ConduitBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(ConduitBlockEntityRenderer.class)
public class MixinConduitBlockEntityRenderer
{
    @Redirect(method = "render(Lnet/minecraft/block/entity/ConduitBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 0))
    private VertexConsumer hookRender(SpriteIdentifier instance, VertexConsumerProvider vertexConsumers, Function<Identifier, RenderLayer> layerFactory)
    {
        return instance.getVertexConsumer(vertexConsumers, RenderLayersClient.ENTITY_SOLID);
    }

    @Redirect(method = "render(Lnet/minecraft/block/entity/ConduitBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 1))
    private VertexConsumer hookRender$1(SpriteIdentifier instance, VertexConsumerProvider vertexConsumers, Function<Identifier, RenderLayer> layerFactory)
    {
        return instance.getVertexConsumer(vertexConsumers, RenderLayersClient.ENTITY_CUTOUT_NO_CULL);
    }

    @Redirect(method = "render(Lnet/minecraft/block/entity/ConduitBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 2))
    private VertexConsumer hookRender$2(SpriteIdentifier instance, VertexConsumerProvider vertexConsumers, Function<Identifier, RenderLayer> layerFactory)
    {
        return instance.getVertexConsumer(vertexConsumers, RenderLayersClient.ENTITY_CUTOUT_NO_CULL);
    }

    @Redirect(method = "render(Lnet/minecraft/block/entity/ConduitBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 3))
    private VertexConsumer hookRender$3(SpriteIdentifier instance, VertexConsumerProvider vertexConsumers, Function<Identifier, RenderLayer> layerFactory)
    {
        return instance.getVertexConsumer(vertexConsumers, RenderLayersClient.ENTITY_CUTOUT_NO_CULL);
    }
}

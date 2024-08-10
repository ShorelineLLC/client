package net.shoreline.client.mixin.render.block.entity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.DecoratedPotBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(DecoratedPotBlockEntityRenderer.class)
public class MixinDecoratedPotBlockEntityRenderer
{
    @Redirect(method = "render(Lnet/minecraft/block/entity/DecoratedPotBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer hookRender(SpriteIdentifier instance, VertexConsumerProvider vertexConsumers, Function<Identifier, RenderLayer> layerFactory)
    {
        return instance.getVertexConsumer(vertexConsumers, RenderLayersClient.ENTITY_SOLID);
    }

    @Redirect(method = "renderDecoratedSide", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer hookRenderDecoratedSide(SpriteIdentifier instance, VertexConsumerProvider vertexConsumers, Function<Identifier, RenderLayer> layerFactory)
    {
        return instance.getVertexConsumer(vertexConsumers, RenderLayersClient.ENTITY_SOLID);
    }
}

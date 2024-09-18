package net.shoreline.client.mixin.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Util;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SequencedMap;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage
{

    @Shadow
    @Final
    private BlockBufferAllocatorStorage blockBufferBuilders;

    @Final
    @Shadow
    @Mutable
    private VertexConsumerProvider.Immediate entityVertexConsumers;

    @Final
    @Shadow
    @Mutable
    private OutlineVertexConsumerProvider outlineVertexConsumers;

    @Mutable
    @Shadow
    @Final
    private VertexConsumerProvider.Immediate effectVertexConsumers;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(int maxBlockBuildersPoolSize, CallbackInfo ci)
    {
        SequencedMap<RenderLayer, BufferAllocator> sequencedMap = (SequencedMap) Util.make(new Object2ObjectLinkedOpenHashMap(), (map) ->
        {
            map.put(TexturedRenderLayers.getEntitySolid(), this.blockBufferBuilders.get(RenderLayer.getSolid()));
            map.put(TexturedRenderLayers.getEntityCutout(), this.blockBufferBuilders.get(RenderLayer.getCutout()));
            map.put(TexturedRenderLayers.getBannerPatterns(), this.blockBufferBuilders.get(RenderLayer.getCutoutMipped()));
            map.put(TexturedRenderLayers.getEntityTranslucentCull(), this.blockBufferBuilders.get(RenderLayer.getTranslucent()));
            map.put(TexturedRenderLayers.getShieldPatterns(), new BufferAllocator(TexturedRenderLayers.getBeds().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getBeds(), new BufferAllocator(TexturedRenderLayers.getBeds().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getShulkerBoxes(), new BufferAllocator(TexturedRenderLayers.getShulkerBoxes().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getSign(), new BufferAllocator(TexturedRenderLayers.getHangingSign().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getHangingSign(), new BufferAllocator(TexturedRenderLayers.getHangingSign().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getChest(), new BufferAllocator(786432));
            map.put(RenderLayer.getArmorEntityGlint(), new BufferAllocator(RenderLayer.getArmorEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getGlint(), new BufferAllocator(RenderLayer.getArmorEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getGlintTranslucent(), new BufferAllocator(RenderLayer.getGlintTranslucent().getExpectedBufferSize()));
            map.put(RenderLayer.getEntityGlint(), new BufferAllocator(RenderLayer.getEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getDirectEntityGlint(), new BufferAllocator(RenderLayer.getDirectEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getWaterMask(), new BufferAllocator(RenderLayer.getWaterMask().getExpectedBufferSize()));
            ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach((renderLayer) ->
            {
                map.put(renderLayer, new BufferAllocator(renderLayer.getExpectedBufferSize()));
            });

            map.put(RenderLayersClient.GLINT, new BufferAllocator(RenderLayersClient.GLINT.getExpectedBufferSize()));
        });
        this.effectVertexConsumers = VertexConsumerProvider.immediate(new BufferAllocator(1536));
        this.entityVertexConsumers = VertexConsumerProvider.immediate(sequencedMap, new BufferAllocator(786432));
        this.outlineVertexConsumers = new OutlineVertexConsumerProvider(this.entityVertexConsumers);
    }
}

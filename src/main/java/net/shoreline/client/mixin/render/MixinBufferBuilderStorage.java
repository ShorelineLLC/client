package net.shoreline.client.mixin.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.Util;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedMap;

@Mixin(BufferBuilderStorage.class)
public class MixinBufferBuilderStorage
{

    @Shadow
    @Final
    private BlockBufferBuilderStorage blockBufferBuilders;

    @Final
    @Shadow
    @Mutable
    private VertexConsumerProvider.Immediate entityVertexConsumers;

    @Final
    @Shadow
    @Mutable
    private OutlineVertexConsumerProvider outlineVertexConsumers;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(int maxBlockBuildersPoolSize, CallbackInfo ci)
    {
        SortedMap sortedMap = Util.make(new Object2ObjectLinkedOpenHashMap(), map ->
        {
            map.put(TexturedRenderLayers.getEntitySolid(), blockBufferBuilders.get(RenderLayer.getSolid()));
            map.put(TexturedRenderLayers.getEntityCutout(), blockBufferBuilders.get(RenderLayer.getCutout()));
            map.put(TexturedRenderLayers.getBannerPatterns(), blockBufferBuilders.get(RenderLayer.getCutoutMipped()));
            map.put(TexturedRenderLayers.getEntityTranslucentCull(), blockBufferBuilders.get(RenderLayer.getTranslucent()));
            map.put(TexturedRenderLayers.getShieldPatterns(), new BufferBuilder(TexturedRenderLayers.getShieldPatterns().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getBeds(), new BufferBuilder(TexturedRenderLayers.getBeds().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getSign(), new BufferBuilder(TexturedRenderLayers.getSign().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getHangingSign(), new BufferBuilder(TexturedRenderLayers.getHangingSign().getExpectedBufferSize()));
            map.put(TexturedRenderLayers.getChest(), new BufferBuilder(786432));
            map.put(RenderLayer.getArmorGlint(), new BufferBuilder(RenderLayer.getArmorGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getArmorEntityGlint(), new BufferBuilder(RenderLayer.getArmorEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getGlint(), new BufferBuilder(RenderLayer.getGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getDirectGlint(), new BufferBuilder(RenderLayer.getDirectGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getGlintTranslucent(), new BufferBuilder(RenderLayer.getGlintTranslucent().getExpectedBufferSize()));
            map.put(RenderLayer.getEntityGlint(), new BufferBuilder(RenderLayer.getEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getDirectEntityGlint(), new BufferBuilder(RenderLayer.getDirectEntityGlint().getExpectedBufferSize()));
            map.put(RenderLayer.getWaterMask(), new BufferBuilder(RenderLayer.getWaterMask().getExpectedBufferSize()));
            map.put(RenderLayersClient.GLINT, new BufferBuilder(RenderLayersClient.GLINT.getExpectedBufferSize()));
            // Liquidbounce b8
//            StoreBufferEvent storeBufferEvent = new StoreBufferEvent(map);
//            EventBus.INSTANCE.dispatch(storeBufferEvent);
//            ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach(renderLayer -> map.put(renderLayer, new BufferBuilder(renderLayer.getExpectedBufferSize())));
        });
        entityVertexConsumers = VertexConsumerProvider.immediate(sortedMap, new BufferBuilder(786432));
        outlineVertexConsumers = new OutlineVertexConsumerProvider(this.entityVertexConsumers);
    }
}

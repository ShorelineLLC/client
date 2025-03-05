package net.shoreline.client.mixin.font;

import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderLayerSet.class)
public class MixinTextRenderLayerSet
{
    @Inject(method = "of", at = @At(value = "HEAD"), cancellable = true)
    private static void hookOf(Identifier textureId, CallbackInfoReturnable<TextRenderLayerSet> cir)
    {
        cir.cancel();
        cir.setReturnValue(new TextRenderLayerSet(RenderLayersClient.TEXT.apply(textureId), RenderLayersClient.TEXT_SEE_THROUGH.apply(textureId), RenderLayersClient.TEXT_POLYGON_OFFSET.apply(textureId)));
    }

    @Inject(method = "ofIntensity", at = @At(value = "HEAD"), cancellable = true)
    private static void hookOfIntensity(Identifier textureId, CallbackInfoReturnable<TextRenderLayerSet> cir)
    {
        cir.cancel();
        cir.setReturnValue(new TextRenderLayerSet(RenderLayersClient.TEXT_INTENSITY.apply(textureId), RenderLayersClient.TEXT_INTENSITY_SEE_THROUGH.apply(textureId), RenderLayersClient.TEXT_INTENSITY_POLYGON_OFFSET.apply(textureId)));
    }
}

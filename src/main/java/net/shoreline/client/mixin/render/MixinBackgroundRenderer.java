package net.shoreline.client.mixin.render;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.shoreline.client.impl.event.render.SkyboxEvent;
import net.shoreline.eventbus.EventBus;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer
{
    @Inject(method = "applyFog", at = @At(value = "RETURN"), cancellable = true)
    private static void hookApplyFog(Camera camera,
                                     BackgroundRenderer.FogType fogType,
                                     Vector4f color,
                                     float viewDistance,
                                     boolean thickenFog,
                                     float tickDelta,
                                     CallbackInfoReturnable<Fog> cir)
    {
        Fog fog = cir.getReturnValue();
        SkyboxEvent.Fog renderFogEvent = new SkyboxEvent.Fog(fogType, viewDistance, fog.start(), fog.end());
        EventBus.INSTANCE.dispatch(renderFogEvent);
        if (renderFogEvent.isCanceled())
        {
            cir.cancel();
            Vector4f color2 = renderFogEvent.getColorVec4();
            cir.setReturnValue(new Fog(renderFogEvent.getFogStart(), renderFogEvent.getFogEnd(), fog.shape(), color2.x, color2.y, color2.z, color2.w));
        }
    }
}

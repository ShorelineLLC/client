package net.shoreline.client.mixin.render;

import net.minecraft.client.render.DimensionEffects;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(DimensionEffects.class)
public class MixinDimensionEffects
{

    /**
     * @param skyAngle
     * @param tickDelta
     * @param cir
     */
    @Inject(method = "getFogColorOverride", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFogColorOverride(float skyAngle, float tickDelta,
                                         CallbackInfoReturnable<float[]> cir)
    {
        SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(tickDelta);
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            Color color = skyboxEvent.getColor();
            cir.cancel();
            cir.setReturnValue(new float[]
                    {
                            (float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
                            (float) color.getBlue() / 255.0f, 1.0f
                    });
        }
    }
}

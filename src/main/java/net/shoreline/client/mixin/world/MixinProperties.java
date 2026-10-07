package net.shoreline.client.mixin.world;

import net.minecraft.client.world.ClientWorld;
import net.shoreline.client.impl.event.render.SkyboxEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public class MixinProperties
{
    @Inject(method = "getHorizonShadingRatio", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetHorizonShadingRatio(CallbackInfoReturnable<Float> cir)
    {
        SkyboxEvent.FogColor fog = new SkyboxEvent.FogColor();
        EventBus.INSTANCE.dispatch(fog);
        if (fog.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(0.0f);
        }
    }
}

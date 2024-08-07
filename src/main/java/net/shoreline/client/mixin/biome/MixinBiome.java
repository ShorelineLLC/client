package net.shoreline.client.mixin.biome;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.world.biome.Biome;
import net.shoreline.eventbus.EventBus;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class MixinBiome
{

    @Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFogColor(CallbackInfoReturnable<Integer> cir)
    {
        SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(0.0f);
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getRGB());
        }
    }
}

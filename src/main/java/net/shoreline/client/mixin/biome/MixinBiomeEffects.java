package net.shoreline.client.mixin.biome;

import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.shoreline.client.impl.event.biome.BiomeEffectsEvent;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BiomeEffects.class)
public class MixinBiomeEffects
{

    /**
     * @param cir
     */
    @Inject(method = "getSkyColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetSkyColor(CallbackInfoReturnable<Integer> cir)
    {
        SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getRGB());
        }
    }

    /**
     * @param cir
     */
    @Inject(method = "getParticleConfig", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetParticleConfig(CallbackInfoReturnable<Optional<BiomeParticleConfig>> cir)
    {
        BiomeEffectsEvent biomeEffectsEvent = new BiomeEffectsEvent();
        EventBus.INSTANCE.dispatch(biomeEffectsEvent);
        if (biomeEffectsEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(Optional.ofNullable(biomeEffectsEvent.getParticleConfig()));
        }
    }
}

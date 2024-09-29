package net.shoreline.client.mixin.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.shoreline.client.impl.event.biome.BiomeColorEvent;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class MixinBiome
{

    @Shadow
    @Final
    private BiomeEffects effects;

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

    @Inject(method = "getWaterColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetWaterColor(CallbackInfoReturnable<Integer> cir)
    {
        BiomeColorEvent.Water waterEvent = new BiomeColorEvent.Water();
        EventBus.INSTANCE.dispatch(waterEvent);
        if (waterEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(waterEvent.getColor());
        }
    }

    @Inject(method = "getWaterFogColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetWaterFogColor(CallbackInfoReturnable<Integer> cir)
    {
        BiomeColorEvent.Water waterEvent = new BiomeColorEvent.Water();
        EventBus.INSTANCE.dispatch(waterEvent);
        if (waterEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(waterEvent.getColor());
        }
    }

    @Inject(method = "getGrassColorAt", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetGrassColorAt(double x, double z, CallbackInfoReturnable<Integer> cir)
    {
        BiomeColorEvent.Grass grassEvent = new BiomeColorEvent.Grass();
        EventBus.INSTANCE.dispatch(grassEvent);
        if (grassEvent.isCanceled())
        {
            cir.cancel();
            int i = effects.getGrassColorModifier().getModifiedGrassColor(x, z, grassEvent.getColor());
            cir.setReturnValue(i);
        }
    }

    @Inject(method = "getDefaultGrassColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetDefaultGrassColor(CallbackInfoReturnable<Integer> cir)
    {
        BiomeColorEvent.Grass grassEvent = new BiomeColorEvent.Grass();
        EventBus.INSTANCE.dispatch(grassEvent);
        if (grassEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(grassEvent.getColor());
        }
    }

    @Inject(method = "getFoliageColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFoliageColor(CallbackInfoReturnable<Integer> cir)
    {
        BiomeColorEvent.Foliage foliageEvent = new BiomeColorEvent.Foliage();
        EventBus.INSTANCE.dispatch(foliageEvent);
        if (foliageEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(foliageEvent.getColor());
        }
    }
}

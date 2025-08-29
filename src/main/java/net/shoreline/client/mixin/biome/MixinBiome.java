package net.shoreline.client.mixin.biome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.shoreline.client.impl.event.render.WorldTintEvent;
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

    @Inject(method = "getWaterColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetWaterColor(CallbackInfoReturnable<Integer> cir)
    {
        WorldTintEvent.Water waterEvent = new WorldTintEvent.Water();
        EventBus.INSTANCE.dispatch(waterEvent);
        if (waterEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(waterEvent.getColor().getRGB());
        }
    }

    @Inject(method = "getWaterFogColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetWaterFogColor(CallbackInfoReturnable<Integer> cir)
    {
        WorldTintEvent.Water waterEvent = new WorldTintEvent.Water();
        EventBus.INSTANCE.dispatch(waterEvent);
        if (waterEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(waterEvent.getColor().getRGB());
        }
    }

    @Inject(method = "getGrassColorAt", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetGrassColorAt(double x, double z, CallbackInfoReturnable<Integer> cir)
    {
        WorldTintEvent.Foliage grassEvent = new WorldTintEvent.Foliage();
        EventBus.INSTANCE.dispatch(grassEvent);
        if (grassEvent.isCanceled())
        {
            cir.cancel();
            int i = effects.getGrassColorModifier().getModifiedGrassColor(x, z, grassEvent.getColor().getRGB());
            cir.setReturnValue(i);
        }
    }

    @Inject(method = "getDefaultGrassColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetDefaultGrassColor(CallbackInfoReturnable<Integer> cir)
    {
        WorldTintEvent.Foliage grassEvent = new WorldTintEvent.Foliage();
        EventBus.INSTANCE.dispatch(grassEvent);
        if (grassEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(grassEvent.getColor().getRGB());
        }
    }

    @Inject(method = "getFoliageColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFoliageColor(CallbackInfoReturnable<Integer> cir)
    {
        WorldTintEvent.Foliage foliageEvent = new WorldTintEvent.Foliage();
        EventBus.INSTANCE.dispatch(foliageEvent);
        if (foliageEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(foliageEvent.getColor().getRGB());
        }
    }
}

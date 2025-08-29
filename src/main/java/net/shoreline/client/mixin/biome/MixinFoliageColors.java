package net.shoreline.client.mixin.biome;

import net.minecraft.world.biome.FoliageColors;
import net.shoreline.client.impl.event.render.WorldTintEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoliageColors.class)
public class MixinFoliageColors
{
    @Inject(method = "getColor", at = @At(value = "HEAD"), cancellable = true)
    private static void hookGetDefaultColor(double temperature, double humidity, CallbackInfoReturnable<Integer> cir)
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

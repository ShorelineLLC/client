package net.shoreline.client.mixin.color.world;

import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.shoreline.client.impl.event.color.world.BiomeColorEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors implements Globals
{
    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private static void hookGetColor(BlockRenderView world, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir)
    {
        if (mc.world == null)
        {
            return;
        }
        BiomeColorEvent biomeColorEvent = new BiomeColorEvent(resolver);
        EventBus.INSTANCE.dispatch(biomeColorEvent);
        if (biomeColorEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(biomeColorEvent.getRGB());
        }
    }
}

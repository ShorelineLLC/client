package net.shoreline.client.mixin.sodium;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.impl.event.render.WorldTintEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Arrays;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.fabric.render.FluidRendererImpl$DefaultRenderContext", remap = false)
public class MixinFluidRendererImplDefaultRenderContext
{
    @Inject(method = "getColorProvider", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetColorProvider(Fluid var1, CallbackInfoReturnable<ColorProvider<FluidState>> cir)
    {
        WorldTintEvent.Lava lavaTintEvent = new WorldTintEvent.Lava();
        EventBus.INSTANCE.dispatch(lavaTintEvent);
        if (var1.getDefaultState().isIn(FluidTags.LAVA))
        {
            cir.setReturnValue((v1, v2, v3, v4, v5, v6) -> getColors(v1, v2, v3, v4, v5, v6, lavaTintEvent.isCanceled(), lavaTintEvent.getColor()));
        }
    }

    @Unique
    public void getColors(LevelSlice slice,
                          BlockPos pos,
                          BlockPos.Mutable scratchPos,
                          FluidState state,
                          ModelQuadView quad,
                          int[] output,
                          boolean canceled,
                          Color color)
    {
        int fillColor = canceled ?
                ColorABGR.pack(color.getBlue(), color.getGreen(), color.getRed(), 255) :
                ColorABGR.pack(15, 85, 205, 255);

        Arrays.fill(output, fillColor);
    }
}

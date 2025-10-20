package net.shoreline.client.mixin.sodium;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.module.world.XRayModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache", remap = false)
public class MixinBlockOcclusionCache
{
    @Inject(
            method = "shouldDrawSide",
            at = @At(value = "HEAD"),
            cancellable = true,
            remap = false
    )
    public void shouldDrawSide(BlockState selfBlockState,
                               BlockView view,
                               BlockPos selfPos,
                               Direction facing,
                               CallbackInfoReturnable<Boolean> cir)
    {
        if (XRayModule.INSTANCE.isEnabled())
        {
            cir.cancel();
            cir.setReturnValue(!XRayModule.INSTANCE.shouldCancelBlockRender(selfBlockState.getBlock()));
        }
    }
}

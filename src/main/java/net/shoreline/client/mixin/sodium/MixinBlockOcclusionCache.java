package net.shoreline.client.mixin.sodium;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.event.render.RenderBlockEvent;
import net.shoreline.client.impl.module.world.XRayModule;
import net.shoreline.eventbus.EventBus;
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
        RenderBlockEvent event = new RenderBlockEvent(selfBlockState);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }
}

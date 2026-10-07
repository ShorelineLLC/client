package net.shoreline.client.mixin.sodium;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.impl.event.render.RenderBlockEvent;
import net.shoreline.client.impl.module.world.XRayModule;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", remap = false)
public class MixinBlockRenderer
{
    @Inject(
            method = "renderModel",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void onRenderModel(BakedModel model,
                               BlockState state,
                               BlockPos pos,
                               BlockPos origin,
                               CallbackInfo ci)
    {
        if (MinecraftClient.getInstance().player != null)
        {
            RenderBlockEvent event = new RenderBlockEvent(state);
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                ci.cancel();
            }
        }
    }
}

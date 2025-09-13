package net.shoreline.client.mixin.world.chunk.light;

import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import net.shoreline.client.impl.event.world.WorldSkylightEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkylightProvider
{
    @Inject(method = "method_51531", at = @At(value = "HEAD"), cancellable = true)
    private void hookRecalculateLevel(long blockPos, long l, int lightLevel, CallbackInfo ci)
    {
        WorldSkylightEvent renderSkylightEvent = new WorldSkylightEvent();
        EventBus.INSTANCE.dispatch(renderSkylightEvent);
        if (renderSkylightEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}

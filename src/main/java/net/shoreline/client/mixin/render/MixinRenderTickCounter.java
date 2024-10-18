package net.shoreline.client.mixin.render;

import net.minecraft.client.render.RenderTickCounter;
import net.shoreline.client.impl.event.render.TickCounterEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @see RenderTickCounter
 * @since 1.0
 */
@Mixin(RenderTickCounter.Dynamic.class)
public class MixinRenderTickCounter
{
    @Shadow
    private float lastFrameDuration;

    @Shadow
    private float tickDelta;

    @Shadow
    private long prevTimeMillis;

    @Final
    @Shadow
    private float tickTime;

    /**
     * @param timeMillis
     * @param cir
     */
    @Inject(method = "beginRenderTick(J)I", at = @At(value = "HEAD"), cancellable = true)
    private void hookBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir)
    {
        TickCounterEvent tickCounterEvent = new TickCounterEvent();
        EventBus.INSTANCE.dispatch(tickCounterEvent);
        if (tickCounterEvent.isCanceled())
        {
            lastFrameDuration = ((timeMillis - prevTimeMillis) / tickTime) * tickCounterEvent.getTicks();
            prevTimeMillis = timeMillis;
            tickDelta += lastFrameDuration;
            int i = (int) tickDelta;
            tickDelta -= i;
            cir.setReturnValue(i);
        }
    }
}

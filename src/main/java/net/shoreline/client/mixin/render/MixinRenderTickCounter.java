package net.shoreline.client.mixin.render;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.render.RenderTickCounter;
import net.shoreline.client.impl.event.render.RenderTickCounterEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class MixinRenderTickCounter
{
    @Shadow
    @Final
    private FloatUnaryOperator targetMillisPerTick;

    @Shadow
    private float dynamicDeltaTicks;

    @Shadow
    private float tickProgress;

    @Shadow
    private long lastTimeMillis;

    @Shadow
    @Final
    private float tickTime;

    @Inject(method = "beginRenderTick(J)I", at = @At(value = "HEAD"), cancellable = true)
    private void hookBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir)
    {
        RenderTickCounterEvent event = new RenderTickCounterEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            this.dynamicDeltaTicks = (float) (timeMillis - this.lastTimeMillis) / this.targetMillisPerTick.apply(this.tickTime);
            this.dynamicDeltaTicks *= event.getTicks();
            this.lastTimeMillis = timeMillis;
            this.tickProgress += this.dynamicDeltaTicks;
            int i = (int) this.tickProgress;
            this.tickProgress -= (float) i;
            cir.setReturnValue(i);
        }
    }
}

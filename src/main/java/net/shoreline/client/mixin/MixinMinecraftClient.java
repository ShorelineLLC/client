package net.shoreline.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient
{
    @Unique
    private long startTime;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTickPre(CallbackInfo ci)
    {
        final TickEvent.Pre tickEvent = new TickEvent.Pre();
        EventBus.INSTANCE.dispatch(tickEvent);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void hookTickPost(CallbackInfo ci)
    {
        final TickEvent.Post tickEvent = new TickEvent.Post();
        EventBus.INSTANCE.dispatch(tickEvent);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void hookRender(boolean tick, CallbackInfo ci)
    {
        startTime = System.nanoTime();
    }
}

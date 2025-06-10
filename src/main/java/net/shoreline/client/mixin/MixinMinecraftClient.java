package net.shoreline.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.event.StageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient
{
    @Unique
    final TickEvent tickEvent = new TickEvent();

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTickPre(CallbackInfo ci)
    {
        tickEvent.setStage(StageEvent.EventStage.PRE);
        EventBus.INSTANCE.dispatch(tickEvent);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void hookTickPost(CallbackInfo ci)
    {
        tickEvent.setStage(StageEvent.EventStage.POST);
        EventBus.INSTANCE.dispatch(tickEvent);
    }
}

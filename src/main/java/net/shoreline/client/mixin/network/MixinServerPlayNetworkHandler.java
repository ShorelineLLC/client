package net.shoreline.client.mixin.network;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.shoreline.client.impl.event.network.ServerTickEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler
{
    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTick(CallbackInfo ci)
    {
        ServerTickEvent serverTickEvent = new ServerTickEvent();
        EventBus.INSTANCE.dispatch(serverTickEvent);
    }
}

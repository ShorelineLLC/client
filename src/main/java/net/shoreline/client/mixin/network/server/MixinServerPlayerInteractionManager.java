package net.shoreline.client.mixin.network.server;

import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.impl.event.network.server.FinishMiningServerEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager
{
    @Inject(method = "finishMining", at = @At(value = "HEAD"), cancellable = true)
    private void hookFinishMining(BlockPos pos, int sequence, String reason, CallbackInfo ci)
    {
        FinishMiningServerEvent finishMiningServerEvent = new FinishMiningServerEvent();
        EventBus.INSTANCE.dispatch(finishMiningServerEvent);
        if (finishMiningServerEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}

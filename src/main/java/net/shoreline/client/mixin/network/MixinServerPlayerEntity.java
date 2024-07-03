package net.shoreline.client.mixin.network;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.shoreline.client.impl.event.world.LoadWorldEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity
{
    @Inject(method = "moveToWorld", at = @At(value = "HEAD"))
    private void hookMoveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir)
    {
        LoadWorldEvent loadWorldEvent = new LoadWorldEvent();
        EventBus.INSTANCE.dispatch(loadWorldEvent);
    }
}

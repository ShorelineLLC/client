package net.shoreline.client.mixin.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.shoreline.client.impl.event.network.ExplosionEvent;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler
{
    @Inject(method = "onPlayerPositionLook", at = @At(value = "HEAD"))
    private void hookPlayerPositionLookPre(PlayerPositionLookS2CPacket packet, CallbackInfo ci)
    {
        RotationUpdateEvent.Pre event = new RotationUpdateEvent.Pre();
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "onPlayerPositionLook", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void hookPlayerPositionLookPrePacket(PlayerPositionLookS2CPacket packet, CallbackInfo ci)
    {
        RotationUpdateEvent.PrePacket event = new RotationUpdateEvent.PrePacket();
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "onPlayerPositionLook", at = @At(value = "TAIL"))
    public void hookPlayerPositionLook(PlayerPositionLookS2CPacket packet,
                                       CallbackInfo ci)
    {
        RotationUpdateEvent event = new RotationUpdateEvent(MinecraftClient.getInstance().player.getYaw(),
                MinecraftClient.getInstance().player.getPitch());
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "onExplosion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;addParticleClient(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void hookExplosion(ExplosionS2CPacket packet, CallbackInfo ci)
    {
        if (packet.playerKnockback().isEmpty())
        {
            return;
        }

        final ExplosionEvent event = new ExplosionEvent(packet.playerKnockback().get());
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
            MinecraftClient.getInstance().player.addVelocityInternal(event.getPlayerVelocity());
        }
    }
}

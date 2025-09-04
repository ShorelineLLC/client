package net.shoreline.client.mixin.network;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.network.EntitySpawnEvent;
import net.shoreline.client.impl.event.network.ExplosionEvent;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.client.impl.imixin.IClientConnection;
import net.shoreline.client.impl.imixin.IClientPlayNetworkHandler;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends MixinClientCommonNetworkHandler implements IClientPlayNetworkHandler
{
    @Shadow
    public abstract ClientConnection getConnection();

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
        RotationUpdateEvent event = new RotationUpdateEvent(client.player.getYaw(), client.player.getPitch());
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "onExplosion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
            shift = At.Shift.AFTER),
            cancellable = true)
    private void hookExplosion(ExplosionS2CPacket packet, CallbackInfo ci)
    {
        final ExplosionEvent event = new ExplosionEvent(packet.center(), packet.playerKnockback().isPresent() ? packet.playerKnockback().get() : Vec3d.ZERO);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
            Vec3d newVelo = event.getPlayerVelocity();
            if (newVelo != null)
            {
                client.player.addVelocityInternal(newVelo);
            }
        }
    }

    @Inject(method = "onEntitySpawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
            shift = At.Shift.AFTER))
    private void hookEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci)
    {
        EntitySpawnEvent event = new EntitySpawnEvent(new Vec3d(packet.getX(), packet.getY(), packet.getZ()),
                packet.getEntityId(), packet.getEntityType());
        EventBus.INSTANCE.dispatch(event);
    }

    @Override
    public void sendQuietPacket(Packet<?> packet)
    {
        ((IClientConnection) getConnection()).hookSendInternal(packet, null, true);
    }
}

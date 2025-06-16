package net.shoreline.client.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection
{
    @Shadow
    @Nullable
    private volatile PacketListener packetListener;

    @Inject(method = "sendImmediately", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendImmediately(Packet<?> packet,
                                     @Nullable PacketCallbacks callbacks,
                                     boolean flush,
                                     CallbackInfo ci)
    {
        PacketEvent.Outbound packetOutboundEvent =
                new PacketEvent.Outbound(packet);
        EventBus.INSTANCE.dispatch(packetOutboundEvent);
        if (packetOutboundEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;" +
            "Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookChannelRead0(ChannelHandlerContext channelHandlerContext,
                                  Packet<?> packet,
                                  CallbackInfo ci)
    {
        PacketListener ownedPacketListener = packetListener;
        if (packet != null && ownedPacketListener != null && ownedPacketListener.accepts(packet))
        {
            PacketEvent.Inbound packetInboundEvent =
                    new PacketEvent.Inbound(packetListener, packet);
            EventBus.INSTANCE.dispatch(packetInboundEvent);
            if (packetInboundEvent.isCanceled())
            {
                ci.cancel();
            }
        }
    }
}

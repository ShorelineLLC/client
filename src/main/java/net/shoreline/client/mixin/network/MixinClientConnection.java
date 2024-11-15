package net.shoreline.client.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.network.DecodePacketEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ClientConnection.class)
public class MixinClientConnection
{
    @Shadow
    @Nullable
    private volatile PacketListener packetListener;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void hookExceptionCaught(ChannelHandlerContext context, Throwable ex, CallbackInfo ci)
    {
        DecodePacketEvent decodePacketEvent = new DecodePacketEvent();
        EventBus.INSTANCE.dispatch(decodePacketEvent);
        if (decodePacketEvent.isCanceled())
        {
            LOGGER.error("Exception caught on network thread:", ex);
            ci.cancel();
        }
    }

    /**
     * @param packet
     * @param callbacks
     * @param ci
     */
    @Inject(method = "sendImmediately", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks,
                                     boolean flush, CallbackInfo ci)
    {
        PacketEvent.Outbound packetOutboundEvent =
                new PacketEvent.Outbound(packet);
        EventBus.INSTANCE.dispatch(packetOutboundEvent);
        if (packetOutboundEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "sendImmediately", at = @At(value = "TAIL"), cancellable = true)
    private void hookSendImmediately$2(Packet<?> packet, @Nullable PacketCallbacks callbacks,
                                       boolean flush, CallbackInfo ci)
    {
        PacketEvent.OutboundPost packetOutboundEvent =
                new PacketEvent.OutboundPost(packet);
        EventBus.INSTANCE.dispatch(packetOutboundEvent);
        if (packetOutboundEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param channelHandlerContext
     * @param packet
     * @param ci
     */
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;" +
            "Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookChannelRead0(ChannelHandlerContext channelHandlerContext,
                                  Packet<?> packet, CallbackInfo ci)
    {
        PacketListener ownedPacketListener = packetListener;
        if (packet != null && ownedPacketListener != null && ownedPacketListener.accepts(packet)) // Josu fix
        {
            PacketEvent.Inbound packetInboundEvent =
                    new PacketEvent.Inbound(packetListener, packet);
            EventBus.INSTANCE.dispatch(packetInboundEvent);
            // prevent client from receiving packet from server
            if (packetInboundEvent.isCanceled())
            {
                ci.cancel();
            }
        }
    }

    /**
     * @param disconnectReason
     * @param ci
     */
    @Inject(method = "disconnect", at = @At(value = "HEAD"))
    private void hookDisconnect(Text disconnectReason, CallbackInfo ci)
    {
        DisconnectEvent disconnectEvent = new DisconnectEvent();
        EventBus.INSTANCE.dispatch(disconnectEvent);
    }
}

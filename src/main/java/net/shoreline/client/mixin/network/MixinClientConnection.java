package net.shoreline.client.mixin.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IClientConnection;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection implements IClientConnection
{
    @Shadow
    @Nullable
    private volatile PacketListener packetListener;

    @Shadow
    private Channel channel;

    @Shadow
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {}

    @Shadow
    private int packetsReceivedCounter;

    @Override
    @Invoker("sendInternal")
    public abstract void hookSendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);

    @Inject(method = "sendImmediately", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendImmediately(Packet<?> packet,
                                     PacketCallbacks callbacks,
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

    @Inject(method = "sendImmediately", at = @At(value = "TAIL"))
    private void hookSendImmediately$1(Packet<?> packet,
                                       PacketCallbacks callbacks,
                                       boolean flush,
                                       CallbackInfo ci)
    {
        PacketEvent.OutboundPost packetOutboundEvent =
                new PacketEvent.OutboundPost(packet);
        EventBus.INSTANCE.dispatch(packetOutboundEvent);
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookChannelRead0(ChannelHandlerContext channelHandlerContext,
                                  Packet<?> packet,
                                  CallbackInfo ci)
    {
        if (!channel.isOpen())
        {
            return;
        }

        PacketListener ownedPacketListener = packetListener;
        if (packet == null || ownedPacketListener == null || !ownedPacketListener.accepts(packet))
        {
            return;
        }

        if (packet instanceof BundleS2CPacket bundlePacket)
        {
            List<Packet<? super ClientPlayPacketListener>> filtered = new LinkedList<>();

            for (Packet<? super ClientPlayPacketListener> packet1 : bundlePacket.getPackets())
            {
                PacketEvent.Inbound packetInboundEvent =
                        new PacketEvent.Inbound(packetListener, packet1, true);
                EventBus.INSTANCE.dispatch(packetInboundEvent);
                if (!packetInboundEvent.isCanceled())
                {
                    filtered.add(packet1);
                }
            }

            ci.cancel();

            try
            {
                if (!filtered.isEmpty())
                {
                    handlePacket(new BundleS2CPacket(filtered), packetListener);
                    ++packetsReceivedCounter;
                }

            } catch (Exception ignored)
            {

            }

            return;
        }

        PacketEvent.Inbound packetInboundEvent =
                new PacketEvent.Inbound(packetListener, packet, false);
        EventBus.INSTANCE.dispatch(packetInboundEvent);
        if (packetInboundEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "TAIL"))
    public void hookChannelRead0Post(ChannelHandlerContext channelHandlerContext,
                                     Packet<?> packet,
                                     CallbackInfo ci)
    {
        PacketEvent.InboundPost packetInboundEvent =
                new PacketEvent.InboundPost(packet);
        EventBus.INSTANCE.dispatch(packetInboundEvent);
    }
}

package net.shoreline.client.impl.network;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.EventBus;

@UtilityClass
public class NetworkUtil
{
    public void receivePacket(Packet<ClientPlayPacketListener> packet)
    {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null)
        {
            return;
        }

        PacketEvent.Inbound event = new PacketEvent.Inbound(handler, packet);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return;
        }

        packet.apply(handler);
    }

    public void disconnect(String disconnectReason)
    {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null)
        {
            MinecraftClient.getInstance().world.disconnect();
            return;
        }

        handler.getConnection().disconnect(Text.of(disconnectReason));
    }
}

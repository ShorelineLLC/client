package net.shoreline.client.impl.network;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IClientWorld;
import net.shoreline.client.impl.imixin.IClientPlayNetworkHandler;
import net.shoreline.eventbus.EventBus;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class NetworkManager extends GenericFeature
{
    private final Set<Packet<?>> sentFromClient = Collections.synchronizedSet(
            Collections.newSetFromMap(new IdentityHashMap<>()));

    public NetworkManager()
    {
        super("Network");
    }

    public void disconnect(String disconnectReason)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null)
        {
            mc.world.disconnect();
            return;
        }

        handler.getConnection().disconnect(Text.of(disconnectReason));
    }

    public void sendPacket(Packet<?> packet)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (mc.world != null && handler != null)
        {
            handler.sendPacket(packet);
            sentFromClient.add(packet);
        }
    }

    public void sendQuietPacket(Packet<?> packet)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (mc.world != null && handler != null)
        {
            ((IClientPlayNetworkHandler) handler).sendQuietPacket(packet);
            sentFromClient.add(packet);
        }
    }

    public void sendSequencedPacket(SequencedPacketCreator packetCreator)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (mc.world == null || handler == null)
        {
            return;
        }

        try (PendingUpdateManager pendingUpdateManager = ((IClientWorld) mc.world).getUpdateManager().incrementSequence())
        {
            int i = pendingUpdateManager.getSequence();
            Packet<ServerPlayPacketListener> packet = packetCreator.predict(i);
            handler.sendPacket(packet);
            sentFromClient.add(packet);
        }
    }

    public void receivePacket(Packet<ClientPlayPacketListener> packet)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
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

        if (mc.isOnThread())
        {
            packet.apply(handler);
        } else
        {
            mc.executeSync(() -> packet.apply(handler));
        }
    }

    public boolean wasSentFromClient(Packet<?> packet)
    {
        return sentFromClient.contains(packet);
    }
}

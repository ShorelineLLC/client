package net.shoreline.client.impl.network;

import lombok.Getter;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.shoreline.client.api.ListeningFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IClientPlayNetworkHandler;
import net.shoreline.client.impl.imixin.IClientWorld;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

public class NetworkManager extends ListeningFeature
{
    @Getter
    private final List<NetworkHandler> handlers = new ArrayList<>();

    private final ConcurrentMap<NetworkHandler, LongAdder> sentCount = new ConcurrentHashMap<>();
    private final Map<Packet<?>, SentPacketData> sentFromClient =
            Collections.synchronizedMap(new ConcurrentHashMap<>());

    public NetworkManager()
    {
        super("Network");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        sentFromClient.clear();
    }
    
    @EventListener
    public void onTick(TickEvent.Post event)
    {
        for (Map.Entry<Packet<?>, SentPacketData> entry : sentFromClient.entrySet())
        {
            SentPacketData data = entry.getValue();
            long timeSince = System.currentTimeMillis() - data.timestamp;
            if (timeSince > 1000)
            {
                sentFromClient.remove(entry.getKey());
                if (sentCount.containsKey(data.handler))
                {
                    sentCount.get(data.handler).decrement();
                }
            }
        }
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

    public void sendPacket(NetworkHandler clientHandler, Packet<?> packet)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (mc.world != null && handler != null)
        {
            handler.sendPacket(packet);
            logPacket(clientHandler, packet);
        }
    }

    public void sendQuietPacket(NetworkHandler clientHandler, Packet<?> packet)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (mc.world != null && handler != null)
        {
            ((IClientPlayNetworkHandler) handler).sendQuietPacket(packet);
            logPacket(clientHandler, packet);
        }
    }

    public void sendSequencedPacket(NetworkHandler clientHandler, SequencedPacketCreator packetCreator)
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
            logPacket(clientHandler, packet);
        }
    }

    private void logPacket(NetworkHandler handler, Packet<?> packet)
    {
        sentFromClient.put(packet, new SentPacketData(handler, System.currentTimeMillis()));
        sentCount.computeIfAbsent(handler, k -> new LongAdder()).increment();
    }

    public void receivePacket(Packet<ClientPlayPacketListener> packet)
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null)
        {
            return;
        }

        PacketEvent.Inbound event = new PacketEvent.Inbound(handler, packet, false);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return;
        }

        runOnThread(() -> packet.apply(handler));
    }

    public int getClientLatency()
    {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null)
        {
            final PlayerListEntry playerEntry = handler.getPlayerListEntry(mc.player.getGameProfile().getId());
            if (playerEntry != null)
            {
                return playerEntry.getLatency();
            }
        }

        return 0;
    }

    public void registerHandler(NetworkHandler h)
    {
        handlers.add(h);
    }

    public long getPacketsSent(NetworkHandler handler)
    {
        return sentCount.getOrDefault(handler, new LongAdder()).longValue();
    }

    public boolean wasSentFromClient(Packet<?> packet)
    {
        return sentFromClient.containsKey(packet);
    }

    private record SentPacketData(NetworkHandler handler, Long timestamp) {}
}

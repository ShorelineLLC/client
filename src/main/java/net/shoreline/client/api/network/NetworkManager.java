package net.shoreline.client.api.network;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.mixin.accessor.AccessorClientWorld;

public class NetworkManager extends GenericFeature
{
    public NetworkManager()
    {
        super("Network");
    }

    public void sendPacket(Packet<?> packet)
    {
        if (mc.world != null && mc.getNetworkHandler() != null)
        {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public void sendSequencedPacket(SequencedPacketCreator packetCreator)
    {
        if (mc.world == null || mc.getNetworkHandler() == null)
        {
            return;
        }
        try (PendingUpdateManager pendingUpdateManager = ((AccessorClientWorld) mc.world).getUpdateManager().incrementSequence())
        {
            int i = pendingUpdateManager.getSequence();
            Packet<ServerPlayPacketListener> packet = packetCreator.predict(i);
            mc.getNetworkHandler().sendPacket(packet);
        }
    }
}

package net.shoreline.client.impl.imixin;

import net.minecraft.network.packet.Packet;

@IMixin
public interface IClientPlayNetworkHandler
{
    void sendQuietPacket(final Packet<?> packet);
}

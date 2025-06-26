package net.shoreline.client.impl.imixin;

import net.minecraft.network.packet.Packet;

@IMixin
public interface IMixinClientPlayNetworkHandler
{
    void sendQuietPacket(final Packet<?> packet);
}

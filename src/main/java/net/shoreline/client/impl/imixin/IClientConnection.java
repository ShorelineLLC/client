package net.shoreline.client.impl.imixin;

import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

@IMixin
public interface IClientConnection
{
    void hookSendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);
}

package net.shoreline.client.security;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;

public class AntiPacketLogger
{
    public void convertMinecraftProtocol(Packet<?> packet)
    {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());

    }
}

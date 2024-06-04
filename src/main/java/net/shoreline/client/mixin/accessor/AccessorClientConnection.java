package net.shoreline.client.mixin.accessor;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface AccessorClientConnection
{

    @Invoker("sendInternal")
    void hookSendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);
}

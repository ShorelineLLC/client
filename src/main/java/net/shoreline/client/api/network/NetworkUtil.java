package net.shoreline.client.api.network;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

@UtilityClass
public class NetworkUtil
{
    public void disconnect(String disconnectReason)
    {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null)
        {
            MinecraftClient.getInstance().world.disconnect(Text.of(disconnectReason));
            return;
        }

        handler.getConnection().disconnect(Text.of(disconnectReason));
    }
}

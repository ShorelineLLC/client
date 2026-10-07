package net.shoreline.client.impl.network;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

@UtilityClass
public class NetworkUtil
{
    public String getServerIp()
    {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        return serverInfo != null ? serverInfo.address : "Singleplayer";
    }
}

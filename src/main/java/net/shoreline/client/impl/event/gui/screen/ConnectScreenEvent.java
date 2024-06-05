package net.shoreline.client.impl.event.gui.screen;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.shoreline.eventbus.event.Event;

public class ConnectScreenEvent extends Event
{

    private final ServerAddress address;
    private final ServerInfo info;

    public ConnectScreenEvent(ServerAddress address, ServerInfo info)
    {
        this.address = address;
        this.info = info;
    }

    public ServerAddress getAddress()
    {
        return address;
    }

    public ServerInfo getInfo()
    {
        return info;
    }
}

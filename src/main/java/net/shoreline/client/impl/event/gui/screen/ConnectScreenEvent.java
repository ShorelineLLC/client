package net.shoreline.client.impl.event.gui.screen;

import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.shoreline.client.api.event.Event;

public class ConnectScreenEvent extends Event {

    private ServerAddress address;
    private ServerInfo info;

    public ConnectScreenEvent(ServerAddress address, ServerInfo info) {
        this.address = address;
        this.info = info;
    }

    public ServerAddress getAddress() {
        return address;
    }

    public ServerInfo getInfo() {
        return info;
    }
}

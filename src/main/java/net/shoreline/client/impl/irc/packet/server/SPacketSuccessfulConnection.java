package net.shoreline.client.impl.irc.packet.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.loader.Loader;

public final class SPacketSuccessfulConnection extends ServerPacket
{
    private final String reconnectionToken;

    public static SPacketSuccessfulConnection newInstance(String packet)
    {
        SPacketSuccessfulConnection packetSuccessfulConnection;
        try
        {
            packetSuccessfulConnection = new SPacketSuccessfulConnection(packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetSuccessfulConnection;
    }

    /**
     * Will exception if the packet string is malformed
     */
    private SPacketSuccessfulConnection(String packet)
    {
        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.reconnectionToken = object.get("Reconnection-Token").getAsString();
    }

    @Override
    public void apply(IRCManager ircManager)
    {
        ircManager.CONNECTED = true;
        ircManager.DID_EVER_CONNECT = true;
        ircManager.setBackupToken(this.reconnectionToken);

        String message = String.format(
                "Successfully %sconnected to the online users network.",
                ircManager.ATTEMPTED_RECONNECT_AT_SOME_POINT ? "re-" : ""
        );

        Loader.info(message);
        ircManager.addToChat(message);
    }
}

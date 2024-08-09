package net.shoreline.server.route.irc.packet.server;

import com.google.gson.JsonObject;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class SPacketDirectMessage extends IRCPacket
{
    private final String message;
    private final LoaderEndpoint.IRCSession senderSession;

    public SPacketDirectMessage(String message,
                                LoaderEndpoint.IRCSession senderSession)
    {
        super("SPacketDirectMessage");

        this.message = message;
        this.senderSession = senderSession;
    }

    @Override
    public void addData(JsonObject obj)
    {
        obj.addProperty("Message", this.message);

        JsonObject sender = new JsonObject();
        sender.addProperty("Username", this.senderSession.getUsername());
        sender.addProperty("User-Type", this.senderSession.getUsertype());

        obj.add("Sender", sender);
    }
}

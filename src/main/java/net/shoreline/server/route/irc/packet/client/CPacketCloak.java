package net.shoreline.server.route.irc.packet.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.server.IRCServer;
import net.shoreline.server.route.loader.LoaderEndpoint;

public final class CPacketCloak extends ClientPacket
{
    private final LoaderEndpoint.IRCSession session;
    private final String cloakType;

    public static CPacketCloak newInstance(LoaderEndpoint.IRCSession session,
                                           String packet)
    {
        CPacketCloak packetCloak;
        try
        {
            packetCloak = new CPacketCloak(session, packet);
        } catch (Throwable t)
        {
            return null;
        }

        return packetCloak;
    }

    public CPacketCloak(LoaderEndpoint.IRCSession session,
                        String packet) throws Throwable
    {
        this.session = session;

        JsonObject object = JsonParser.parseString(packet).getAsJsonObject();

        this.cloakType = object.get("Cloak-Type").getAsString();
    }

    @Override
    public void apply(IRCServer server)
    {
        if (this.session.getUsertype().equals("release"))
        {
            server.sendServerMessage("You do not have access to this command.", this.session);
            return;
        }

        String code;
        switch (this.cloakType.toLowerCase())
        {
            case "dev" -> {
                if (this.session.getUsertype().equals("beta"))
                {
                    server.sendServerMessage("You do not have permission to mask as \u00a7cdev\u00a7r.", this.session);
                }

                code = "c";
                this.session.setMaskedUserType("dev");
            }
            case "beta" -> {
                code = "9";
                this.session.setMaskedUserType("beta");
            }
            case "release" -> {
                code = "f";
                this.session.setMaskedUserType("release");
            }
            default -> {
                server.sendServerMessage("Unrecognized usertype: " + this.cloakType, this.session);
                return;
            }
        }

        server.sendServerMessage("Cloaked your rank to \u00a7" + code + this.cloakType + "\u00a7r" + ".", this.session);
    }

    @Override
    public IRCPacket getResponse(IRCServer server)
    {
        return null;
    }
}

package net.shoreline.server.route.irc.server;

import io.javalin.websocket.*;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.irc.packet.ClientPacket;
import net.shoreline.server.route.irc.packet.IRCPacket;
import net.shoreline.server.route.irc.packet.client.CPacketPing;
import net.shoreline.server.route.irc.packet.server.*;
import net.shoreline.server.route.loader.LoaderEndpoint;
import net.shoreline.server.token.TokenManager;
import org.eclipse.jetty.websocket.api.exceptions.WebSocketTimeoutException;

import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.concurrent.*;

public final class IRCServer
{
    private final ConcurrentHashMap<LoaderEndpoint.IRCSession, WsContext> sessionMap = new ConcurrentHashMap<>();
    // A map of users to their connected servers
    private final ConcurrentHashMap<String, CPacketPing.OnlineUser> onlineUsers = new ConcurrentHashMap<>();

    private final Set<String> muted = ConcurrentHashMap.newKeySet();

    public final List<String> authorizedBackupTokens = new ArrayList<>();

    // Rate limiting
    public final ConcurrentHashMap<String, Integer> packetCountMap = new ConcurrentHashMap<>();
    private final Set<String> alerted = new ConcurrentSkipListSet<>();

    private final int MESSAGE_LIMIT = 10;

    public IRCServer()
    {
        // Create service to monitor rate limiting
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() ->
        {
            this.packetCountMap.clear();
            this.alerted.clear();
        }, 10, 10, TimeUnit.SECONDS);
    }

    public WsConnectHandler onConnect()
    {
        return ws ->
        {
            String token = ws.queryParam("token");

            if (token == null)
            {
                ws.session.close();
                return;
            }

            if (!LoaderEndpoint.IRC_TOKEN_MANAGER.isAuthorizedToken(token) && !this.authorizedBackupTokens.contains(token))
            {
                LoaderEndpoint.awaitingTokens.remove(token);

                SPacketDisconnect disconnect = new SPacketDisconnect(
                        "Your IRC token is no longer valid. You will need to restart your game for IRC functionality.", true
                );

                ws.send(disconnect.fullySerialize());
                ws.session.close();
                return;
            }

            LoaderEndpoint.IRCSession session = LoaderEndpoint.awaitingTokens.remove(token);
            boolean backup = this.authorizedBackupTokens.remove(token);

            if (session == null)
            {
                SPacketDisconnect disconnect = new SPacketDisconnect(
                        "Your IRC session was not loaded properly. You will need to restart your game for IRC functionality.", true
                );

                ws.send(disconnect.fullySerialize());
                ws.session.close();
                return;
            }

            this.sessionMap.put(session, ws);

            // remove any previous backup tokens associated with this user
            Iterator<Map.Entry<String, LoaderEndpoint.IRCSession>> iter = LoaderEndpoint.awaitingTokens.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry<String, LoaderEndpoint.IRCSession> entry = iter.next();
                if (entry.getValue().equals(session))
                {
                    this.authorizedBackupTokens.remove(entry.getKey());
                    iter.remove();
                }
            }

            String backupToken = TokenManager.generateRandomToken();
            this.authorizedBackupTokens.add(backupToken);

            LoaderEndpoint.awaitingTokens.put(backupToken, session);

            SPacketSuccessfulConnection packet = new SPacketSuccessfulConnection(
                    backupToken
            );

            ws.send(packet.fullySerialize());

            ServerMain.LOGGER.info("Successfully {} {} to the network", backup ? "re-connected" : "connected" ,session.getUsername());
        };
    }

    public WsMessageHandler onMessage()
    {
        return ws ->
        {
            String sessionId = ws.sessionId();
            int messageCount = this.packetCountMap.getOrDefault(sessionId, 0);

            if (messageCount >= this.MESSAGE_LIMIT)
            {
                if (!this.alerted.contains(sessionId))
                {
                    ws.send(new SPacketRateLimit().fullySerialize());
                    this.alerted.add(sessionId);
                }

                return;
            }

            LoaderEndpoint.IRCSession session = this.sessionMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(ws))
                    .findAny()
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (session == null)
            {
                SPacketDisconnect disconnect = new SPacketDisconnect(
                        "You do not have a registered IRC session. You will need to restart your game for IRC functionality.", true
                );
                ws.send(disconnect.fullySerialize());

                ServerMain.LOGGER.warn("Received a message from a client without a registered session. This shouldn't happen");
                ws.session.close();
                return;
            }

            String message = ws.message();
            ClientPacket packet = ClientPacket.deserializeClientPacket(sessionId, session, message);

            if (packet == null)
            {
                SPacketDisconnect disconnect = new SPacketDisconnect(
                        "Sent an invalid packet.", true
                );

                ws.send(disconnect.fullySerialize());
                ServerMain.LOGGER.warn("Received a malformed packet from {}: {}", session.getUsername(), message);
                ws.session.close();
                return;
            }

            if (!(packet instanceof CPacketPing)) // ping packets don't count for rate limiting, hopefully this doesn't bite me
            {
                this.packetCountMap.put(sessionId, messageCount + 1);
            }

            packet.apply(this);

            IRCPacket response = packet.getResponse(this);
            if (response != null)
            {
                ws.send(response.fullySerialize());
            }
        };
    }

    public WsBinaryMessageHandler onBinaryMessage()
    {
        return ws ->
        {
        };
    }

    public WsCloseHandler onClose()
    {
        return ws ->
        {
            String sessionId = ws.sessionId();

            LoaderEndpoint.IRCSession session = this.sessionMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(ws))
                    .findAny()
                    .map(Map.Entry::getKey)
                    .orElse(null);

            this.onlineUsers.remove(sessionId);

            if (session != null)
            {
                this.sessionMap.remove(session);
                ServerMain.LOGGER.info("{} disconnected from the network", session.getUsername());
            } else
            {
                ServerMain.LOGGER.info("Blocked a user from entering the IRC because the token was expired");
            }
        };
    }

    public WsErrorHandler onError()
    {
        return ws ->
        {
            if (!(ws.error() instanceof ClosedChannelException || ws.error() instanceof WebSocketTimeoutException))
            {
                ServerMain.LOGGER.error("IRC Error:", ws.error());
            }
        };
    }

    public void broadcastUserMessage(String message,
                                     LoaderEndpoint.IRCSession senderSession)
    {
        SPacketChatMessage chatMessage = new SPacketChatMessage(message, senderSession);

        this.sessionMap.forEach((ircSession, wsContext) ->
        {
            if (!ircSession.equals(senderSession))
            {
                wsContext.send(chatMessage.fullySerialize());
            }
        });
    }

    public void broadcastServerMessage(String message,
                                       boolean ignoreOffUsers)
    {
        SPacketServerMessage packet = new SPacketServerMessage(message);
        this.sessionMap.forEach((ircSession, wsContext) -> {
            if (ignoreOffUsers && !ircSession.isChatEnabled())
            {
                return;
            }

            wsContext.send(packet.fullySerialize());
        });
    }

    public void sendPacket(IRCPacket packet,
                           LoaderEndpoint.IRCSession session)
    {
        WsContext context = this.sessionMap.get(session);

        if (context != null)
        {
            context.send(packet.fullySerialize());
        } else
        {
            ServerMain.LOGGER.error("Tried to send a packet to a null WsContext. Target session={}", session.getUsername());
        }
    }

    public void sendServerMessage(String message,
                                  LoaderEndpoint.IRCSession session)
    {
        SPacketServerMessage serverMessage = new SPacketServerMessage(message);
        sendPacket(serverMessage, session);
    }

    public void sendPrivateMessage(String message,
                                   LoaderEndpoint.IRCSession session,
                                   LoaderEndpoint.IRCSession senderSession)
    {
        SPacketDirectMessage chatMessage = new SPacketDirectMessage(message, senderSession);
        sendPacket(chatMessage, session);
    }

    public void updateOnlineUser(String sessionId,
                                 CPacketPing.OnlineUser user)
    {
        this.onlineUsers.put(sessionId, user);
    }

    public List<CPacketPing.OnlineUser> getAllUsers()
    {
        return this.sessionMap.keySet().stream()
                .map(session -> new CPacketPing.OnlineUser(session, null, null, null))
                .toList();
    }

    public List<CPacketPing.OnlineUser> getUsersConnectedTo(String serverIp)
    {
        return this.onlineUsers.values().stream()
                .filter(combo -> combo.currentServer().equals(serverIp))
                .toList();
    }

    public LoaderEndpoint.IRCSession getSessionByUsername(String username)
    {
        for (Map.Entry<LoaderEndpoint.IRCSession, WsContext> entry : this.sessionMap.entrySet())
        {
            if (entry.getKey().getUsername().equals(username))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public boolean isMuted(LoaderEndpoint.IRCSession session)
    {
        String username = session.getUsername();
        return this.muted.contains(username);
    }

    public boolean setMuted(LoaderEndpoint.IRCSession session,
                            boolean muted)
    {
        String username = session.getUsername();
        if (muted)
        {
            return this.muted.add(username);
        } else
        {
            return this.muted.remove(username);
        }
    }
}

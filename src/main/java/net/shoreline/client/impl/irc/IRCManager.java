package net.shoreline.client.impl.irc;

import net.shoreline.client.impl.irc.packet.IRCPacket;
import net.shoreline.client.impl.irc.packet.ServerPacket;
import net.shoreline.client.impl.irc.packet.client.CPacketPing;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.loader.Loader;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public final class IRCManager implements Globals
{
    private static IRCManager instance;

    public final ScheduledExecutorService pinger;
    public final ScheduledExecutorService manager;

    private String backupToken;

    private OnlineUser lastMessagedUser;
    private OnlineUser lastMessageReceivedUser;

    // Default as disconnected until the server sends an SPacketSuccessfulConnection
    public boolean CONNECTED = false;
    public boolean DID_EVER_CONNECT = false;
    public boolean ATTEMPTED_RECONNECT_AT_SOME_POINT = false;

    public boolean FULLY_KILLED = false;

    public boolean MUTED = false;


    private final Queue<IRCPacket> sendQueue = new ConcurrentLinkedQueue<>();

    /**
     * A list of users using Shoreline on the CURRENT SERVER YOU ARE ON, NOT globally.
     */
    private final Set<OnlineUser> activeOnlineUsers = ConcurrentHashMap.newKeySet();

    /**
     * A list of global Shoreline users
     */
    private final Set<OnlineUser> allOnlineUsers = ConcurrentHashMap.newKeySet();

    private IRCManager()
    {
        this.pinger = Executors.newScheduledThreadPool(1);
        this.manager = Executors.newScheduledThreadPool(1);

        // Schedule ping packets
        this.pinger.scheduleAtFixedRate(() ->
        {
            CPacketPing packetPing = createPingPacket();
            sendPacket(packetPing);
        }, 0, 5, TimeUnit.SECONDS);

        // Schedule queue flush and read incoming
        this.manager.scheduleAtFixedRate(() ->
        {
            for (String incoming : readIncoming())
            {
                if (FULLY_KILLED)
                {
                    return;
                }

                ServerPacket packet = ServerPacket.deserializeServerPacket(incoming);

                if (packet != null)
                {
                    packet.apply(this);
                } else
                {
                    Loader.error("Received an unknown server packet: {}", incoming);
                }
            }

            while (!this.sendQueue.isEmpty())
            {
                IRCPacket packet = this.sendQueue.poll();
                dispatchPacket(packet.fullySerialize());
                packet.onSend(this);
            }
        }, 0, 250, TimeUnit.MILLISECONDS);
    }

    public void sendPacket(IRCPacket packet)
    {
        if (FULLY_KILLED)
        {
            return;
        }

        if (!CONNECTED && DID_EVER_CONNECT)
        {
            ATTEMPTED_RECONNECT_AT_SOME_POINT = true;
            attemptReconnection(this.backupToken);
            return;
        }

        this.sendQueue.add(packet);
    }

    public void sendPingPacket()
    {
        sendPacket(createPingPacket());
    }

    private native void dispatchPacket(String packet);

    private native void attemptReconnection(String backupToken);

    private native List<String> readIncoming();

    public OnlineUser findOnlineUser(String playerName)
    {
        return this.activeOnlineUsers.stream()
                .filter(onlineUser -> onlineUser.getName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    public OnlineUser findGlobalOnlineUser(String username)
    {
        return this.allOnlineUsers.stream()
                .filter(onlineUser -> onlineUser.getName().equals(username))
                .findFirst()
                .orElse(null);
    }

    public Set<OnlineUser> getActiveOnlineUsers()
    {
        return this.activeOnlineUsers;
    }

    public Set<OnlineUser> getAllOnlineUsers()
    {
        return this.allOnlineUsers;
    }

    public void clearOutboundPackets()
    {
        this.sendQueue.clear();
    }

    public void addToChat(String message)
    {
        mc.send(() ->
        {
            if (mc.player != null && mc.world != null)
            {
                ChatUtil.clientSendMessageRaw("§s[IRC]§7 %s", message);
            }
        });
    }

    public void addServerMessageToChat(String message)
    {
        mc.send(() ->
        {
            if (mc.player != null && mc.world != null)
            {
                ChatUtil.clientSendMessageRaw("§s[IRC/Server]§7 %s", message);
            }
        });
    }

    public void setLastMessagedUser(OnlineUser onlineUser)
    {
        this.lastMessagedUser = onlineUser;
    }

    public OnlineUser getLastMessagedUser()
    {
        return lastMessagedUser;
    }

    public void setLastMessageReceivedUser(OnlineUser onlineUser)
    {
        this.lastMessageReceivedUser = onlineUser;
    }

    public OnlineUser getLastMessageReceivedUser()
    {
        return lastMessageReceivedUser;
    }

    public void setBackupToken(String token)
    {
        this.backupToken = token;
    }

    private CPacketPing createPingPacket()
    {
        String currentUsername = mc.getSession().getUsername();

        String currentServerName = "null";
        if (mc.getNetworkHandler() != null && mc.getCurrentServerEntry() != null)
        {
            currentServerName = mc.getCurrentServerEntry().address;
        }

        return new CPacketPing(currentUsername, currentServerName);
    }

    public static IRCManager getInstance()
    {
        if (instance == null)
        {
            instance = new IRCManager();
        }

        return instance;
    }
}

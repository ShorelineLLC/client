package net.shoreline.client.impl.manager.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.shoreline.client.impl.event.gui.screen.ConnectScreenEvent;
import net.shoreline.client.impl.event.network.ConnectionEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IClientPlayNetworkHandler;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorClientWorld;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.*;

/**
 * @author linus
 * @since 1.0
 */
public class NetworkManager implements Globals
{
    private static final Set<Packet<?>> PACKET_CACHE = new HashSet<>();

    private ServerAddress address;
    private ServerInfo info;

    public NetworkManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onConnect(ConnectScreenEvent event)
    {
        address = event.getAddress();
        info = event.getInfo();
    }

    /**
     * @param event
     */
    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        PACKET_CACHE.clear();
    }

    @EventListener
    public void onPlayerListS2CCPacketEntry(PacketEvent.Inbound event)
    {
        if (!(event.getPacket() instanceof PlayerListS2CPacket) || mc.world == null)
        {
            return;
        }
        PlayerListS2CPacket packet = (PlayerListS2CPacket) event.getPacket();
        if (!(packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)))
        {
            return;
        }
        packet.getEntries().stream()
                .filter(data -> data != null && data.profile() != null)
                .filter(data -> data.profile().getName() != null && !data.profile().getName().isEmpty() || data.profile().getId() != null)
                .forEach(data ->
                {
                    String name = data.profile().getName();
                    UUID uuid = data.profile().getId();
                    PlayerEntity playerEntity = mc.world.getPlayerByUuid(uuid);
                    if (name == null && playerEntity == null)
                    {
                        if (uuid == null)
                        {
                            return;
                        }
                        String lookupName = Managers.LOOKUP.getNameFromUUID(uuid);
                        if (lookupName != null)
                        {
                            name = lookupName;
                        }
                    }
                    EventBus.INSTANCE.dispatch(new ConnectionEvent.JoinEvent(playerEntity, name, uuid));
                });
    }

    @EventListener
    public void onPlayerRemoveS2CPacket(PacketEvent.Inbound event)
    {
        if (!(event.getPacket() instanceof PlayerRemoveS2CPacket))
        {
            return;
        }
        PlayerRemoveS2CPacket packet = (PlayerRemoveS2CPacket) event.getPacket();
        for (UUID uuid : packet.profileIds())
        {
            if (mc.getNetworkHandler() == null)
            {
                return;
            }
            List<PlayerListEntry> infoMap = new ArrayList<>(mc.getNetworkHandler().getPlayerList());
            String name = null;
            for (PlayerListEntry info : infoMap)
            {
                GameProfile gameProfile = info.getProfile();
                if (gameProfile.getId().equals(uuid))
                {
                    name = gameProfile.getName();
                }
            }
            if (name != null)
            {
                EventBus.INSTANCE.dispatch(new ConnectionEvent.LeaveEvent(name, uuid));
            }
        }
    }

    public void connect(final ServerAddress address, final ServerInfo info)
    {
        if (mc.getNetworkHandler() == null)
        {
            return;
        }
        mc.getNetworkHandler().getConnection().connect(address.getAddress(), address.getPort(), new ClientLoginNetworkHandler(mc.getNetworkHandler().getConnection(), mc, info, null, false, null, null));
    }

    /**
     * @param p
     */
    public void sendPacket(final Packet<?> p)
    {
        if (mc.getNetworkHandler() != null)
        {
            PACKET_CACHE.add(p);
            mc.getNetworkHandler().sendPacket(p);
        }
    }

    public void sendQuietPacket(final Packet<?> p)
    {
        if (mc.getNetworkHandler() != null)
        {
            PACKET_CACHE.add(p);
            ((IClientPlayNetworkHandler) mc.getNetworkHandler()).sendQuietPacket(p);
        }
    }

    /**
     * @param p
     */
    public void sendSequencedPacket(final SequencedPacketCreator p)
    {
        if (mc.world != null)
        {
            PendingUpdateManager updater =
                    ((AccessorClientWorld) mc.world).hookGetPendingUpdateManager().incrementSequence();
            try
            {
                int i = updater.getSequence();
                Packet<ServerPlayPacketListener> packet = p.predict(i);
                sendPacket(packet);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                if (updater != null)
                {
                    try
                    {
                        updater.close();
                    }
                    catch (Throwable e1)
                    {
                        e1.printStackTrace();
                        e.addSuppressed(e1);
                    }
                }
                throw e;
            }
            if (updater != null)
            {
                updater.close();
            }
        }
    }

    /**
     * @return
     */
    public int getClientLatency()
    {
        if (mc.getNetworkHandler() != null)
        {
            final PlayerListEntry playerEntry =
                    mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().getId());
            if (playerEntry != null)
            {
                return playerEntry.getLatency();
            }
        }
        return 0;
    }

    public ServerAddress getAddress()
    {
        return address;
    }

    public void setAddress(ServerAddress address)
    {
        this.address = address;
    }

    public ServerInfo getInfo()
    {
        return info;
    }

    public void setInfo(ServerInfo info)
    {
        this.info = info;
    }

    public boolean isCrystalPvpCC()
    {
        return getServerIp().contains("crystalpvp.cc");
    }

    public boolean isGrimCC()
    {
        return getServerIp().equalsIgnoreCase("grim.crystalpvp.cc");
    }

    public String getServerIp()
    {
        if (info != null)
        {
            return info.address;
        }
        return "Singleplayer";
    }

    /**
     * @param p
     * @return
     */
    public boolean isCached(Packet<?> p)
    {
        return PACKET_CACHE.contains(p);
    }
}

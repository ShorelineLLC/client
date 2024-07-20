package net.shoreline.client.impl.event.network;

import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.eventbus.event.Event;

import java.util.UUID;

/**
 * @author hockeyl8
 * @since 1.0
 */
public class ConnectionEvent extends Event
{
    private final PlayerEntity playerEntity;
    private final String username;
    private final UUID uuid;

    private ConnectionEvent(PlayerEntity playerEntity, String username, UUID uuid)
    {
        this.playerEntity = playerEntity;
        this.username = username;
        this.uuid = uuid;
    }

    public PlayerEntity getPlayerEntity()
    {
        return playerEntity;
    }

    public String getUsername()
    {
        return username;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public static class JoinEvent extends ConnectionEvent
    {
        public JoinEvent(PlayerEntity playerEntity, String username, UUID uuid)
        {
            super(playerEntity, username, uuid);
        }
    }

    public static class LeaveEvent extends ConnectionEvent
    {
        public LeaveEvent(String username, UUID uuid)
        {
            super(null, username, uuid);
        }
    }
}

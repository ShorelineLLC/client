package net.shoreline.client.impl.event.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.manager.client.cape.CapeType;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class CapesEvent extends Event
{
    private final GameProfile gameProfile;
    private Identifier texture;

    public CapesEvent(GameProfile gameProfile)
    {
        this.gameProfile = gameProfile;
    }

    public GameProfile getGameProfile()
    {
        return gameProfile;
    }

    public void setTexture(Identifier texture)
    {
        this.texture = texture;
    }

    public Identifier getTexture()
    {
        return texture;
    }
}

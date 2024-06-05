package net.shoreline.client.impl.event.network;

import com.mojang.authlib.GameProfile;
import net.shoreline.client.impl.manager.client.cape.CapeManager;
import net.shoreline.eventbus.event.Event;

public class LoadCapeEvent extends Event
{
    private final GameProfile profile;
    private final CapeManager.CapeTexture texture;

    public LoadCapeEvent(GameProfile profile, CapeManager.CapeTexture texture)
    {
        this.profile = profile;
        this.texture = texture;
    }

    public CapeManager.CapeTexture getTexture()
    {
        return texture;
    }

    public GameProfile getGameProfile()
    {
        return profile;
    }
}

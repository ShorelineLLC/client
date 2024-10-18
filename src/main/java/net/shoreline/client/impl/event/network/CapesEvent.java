package net.shoreline.client.impl.event.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Identifier;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class CapesEvent extends Event
{
    private final GameProfile gameProfile;
    private Identifier texture;
    private boolean optifine;

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

    public void setShowOptifine(boolean optifine)
    {
        this.optifine = optifine;
    }

    public boolean getShowOptifine()
    {
        return optifine;
    }
}

package net.shoreline.client.impl.event.render;

import net.shoreline.client.mixin.render.MixinLightmapTextureManager;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @see MixinLightmapTextureManager
 * @since 1.0
 */
@Cancelable
public class LightmapGammaEvent extends Event
{
    //
    private int gamma;

    /**
     * @param gamma
     */
    public LightmapGammaEvent(int gamma)
    {
        this.gamma = gamma;
    }

    public int getGamma()
    {
        return gamma;
    }

    public void setGamma(int gamma)
    {
        this.gamma = gamma;
    }
}

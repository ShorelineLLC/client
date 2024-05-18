package net.shoreline.client.impl.event.buffers;

import net.minecraft.client.render.RenderLayer;
import net.shoreline.client.api.render.RenderLayersClient;

public class RenderLayersBuffer {

    public static RenderLayer getGlint()
    {
        return RenderLayersClient.GLINT;
    }
}

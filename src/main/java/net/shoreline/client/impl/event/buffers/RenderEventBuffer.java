package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.api.render.RenderBuffers;

public class RenderEventBuffer {

    public static void hookRenderBufferPre()
    {
        RenderBuffers.preRender();
    }

    public static void hookRenderBufferPost()
    {
        RenderBuffers.postRender();
    }
}

package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.eventbus.annotation.EventListener;

public class NametagsModule extends RenderModule
{
    private static NametagsModule INSTANCE;

    public NametagsModule()
    {
        super("Nametags", "Adds info to player nametags", GuiCategory.RENDER);
    }

    public static NametagsModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.gameRenderer == null || mc.getCameraEntity() == null)
        {
            return;
        }

        
    }
}

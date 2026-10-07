package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.screen.DeathScreen;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.OpenScreenEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoRespawnModule extends Toggleable
{
    private boolean respawn;

    public AutoRespawnModule()
    {
        super("AutoRespawn", "Respawns immediately after death", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (!checkNull() && respawn && mc.player.isDead())
        {
            mc.player.requestRespawn();
            respawn = false;
        }
    }

    @EventListener
    public void onOpenScreen(OpenScreenEvent event)
    {
        if (event.getScreen() instanceof DeathScreen)
        {
            respawn = true;
        }
    }
}

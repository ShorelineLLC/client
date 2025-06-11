package net.shoreline.client.api.macro;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class MacroManager
{
    public MacroManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTick(TickEvent event)
    {

    }
}

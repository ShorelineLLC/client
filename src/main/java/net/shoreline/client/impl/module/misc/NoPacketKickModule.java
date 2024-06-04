package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.DecodePacketEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class NoPacketKickModule extends ToggleModule
{

    /**
     *
     */
    public NoPacketKickModule()
    {
        super("NoPacketKick", "Prevents getting kicked by packets", ModuleCategory.MISCELLANEOUS);
    }

    // TODO: Add more packet kick checks
    @EventListener
    public void onDecodePacket(DecodePacketEvent event)
    {
        event.cancel();
    }
}

package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.impl.module.client.CapesModule;
import net.shoreline.client.impl.module.client.HUDModule;
import net.shoreline.client.impl.module.misc.BetterChatModule;
import net.shoreline.client.impl.module.movement.NoSlowModule;

public class ModulesBuffer {

    public static CapesModule getCapesModule() {
        return CapesModule.INSTANCE;
    }

    public static NoSlowModule getNoSlowModule() {
        return NoSlowModule.INSTANCE;
    }

    public static HUDModule getHudModule() {
        return HUDModule.INSTANCE;
    }

    public static BetterChatModule getBetterChatModule() {
        return BetterChatModule.INSTANCE;
    }
}

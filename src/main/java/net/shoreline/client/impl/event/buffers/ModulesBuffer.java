package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.impl.module.client.CapesModule;
import net.shoreline.client.impl.module.client.HUDModule;
import net.shoreline.client.impl.module.misc.BetterChatModule;
import net.shoreline.client.impl.module.movement.NoSlowModule;
import net.shoreline.client.init.Modules;

public class ModulesBuffer {

    public static CapesModule getCapesModule() {
        return Modules.CAPES;
    }

    public static NoSlowModule getNoSlowModule() {
        return Modules.NO_SLOW;
    }

    public static HUDModule getHudModule() {
        return Modules.HUD;
    }

    public static BetterChatModule getBetterChatModule() {
        return Modules.BETTER_CHAT;
    }
}

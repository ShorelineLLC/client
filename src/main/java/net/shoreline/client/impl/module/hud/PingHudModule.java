package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.HeadlessMCModule;
import net.shoreline.client.impl.module.client.LatencyModule;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.render.ClientFormatting;

public class PingHudModule extends DynamicHudModule
{
    public PingHudModule()
    {
        super("Ping", "Displays current server latency", 200, 250);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getLatencyText, () -> !mc.isInSingleplayer()));
    }

    public String getLatencyText()
    {
        String headless = HeadlessMCModule.INSTANCE.isEnabled() ? Formatting.RESET + " Headless " + Formatting.WHITE + "0ms" : "";
        int latency = LatencyModule.INSTANCE.isEnabled() ? LatencyModule.INSTANCE.getCurrentLatency() : Managers.NETWORK.getClientLatency();
        return String.format("Ping " + Formatting.WHITE + "%dms" + headless, latency);
    }
}

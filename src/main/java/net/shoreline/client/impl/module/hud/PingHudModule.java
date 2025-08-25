package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.HeadlessMCModule;
import net.shoreline.client.impl.module.exploit.FastLatencyModule;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.headless.HeadlessAPI;

public class PingHudModule extends DynamicHudModule
{
    public PingHudModule()
    {
        super("Ping", "Displays current server latency", 200, 250);
    }

    @Override
    public void onEnable()
    {
        getHudEntries().add(new DynamicEntry(this, this::getLatencyText, () -> !mc.isInSingleplayer()));
    }

    public String getLatencyText()
    {
        Formatting connectionColor = HeadlessAPI.isConnected() ? Formatting.GREEN : Formatting.RED;
        String headless = HeadlessMCModule.INSTANCE.isEnabled() ? connectionColor + " Headless " + Formatting.WHITE + "0ms" : "";
        int latency = FastLatencyModule.INSTANCE.isEnabled() ? FastLatencyModule.INSTANCE.getCurrentLatency() : Managers.NETWORK.getClientLatency();
        return String.format("Ping " + Formatting.WHITE + "%dms" + headless, latency);
    }
}

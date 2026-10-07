package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;

import java.text.DecimalFormat;

public class TPSHudModule extends DynamicHudModule
{
    public TPSHudModule()
    {
        super("TPS", "Displays current server ticks", 200, 225);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getTPSText, () -> true));
    }

    public String getTPSText()
    {
        double curr = Managers.TICK.getLatestTPS();
        double avg = Managers.TICK.getAverageTPS();
        return String.format("TPS " + Formatting.WHITE + "%s " + Formatting.GRAY + "[" + Formatting.WHITE + "%s" + Formatting.GRAY + "]",
            DECIMAL_TRIMMED.format(curr) + (Managers.TICK.getSize() == 20 ? "" : "*"),
            DECIMAL_TRIMMED.format(avg));
    }
}

package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.render.ClientFormatting;

public class CoordsHudModule extends DynamicHudModule
{
    Config<Boolean> netherConfig = new BooleanConfig.Builder("Nether")
            .setDescription("Show nether coordinates")
            .setDefaultValue(true).build();
    Config<Boolean> showDecimal = new BooleanConfig.Builder("ShowDecimal")
            .setDescription("Shows exact decimal coordinates")
            .setDefaultValue(false).build();

    public CoordsHudModule()
    {
        super("Coords", "Displays the player coordinates", 200, 400);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getCoordsText, () -> true));
    }

    public String getCoordsText()
    {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        boolean nether = mc.world.getRegistryKey() == World.NETHER;
        double nX = nether ? x * 8 : x / 8;
        double nZ = nether ? z * 8 : z / 8;
        return String.format("XYZ " + Formatting.WHITE + "%s, %s, %s " + (netherConfig.getValue() ? ClientFormatting.THEME + "(" + Formatting.WHITE + "%s, %s" + ClientFormatting.THEME + ")" : ""),
                format(x), format(y), format(z), format(nX), format(nZ));
    }

    public String format(double n)
    {
        return showDecimal.getValue() ? DECIMAL.format(n) : WHOLE.format(n);
    }
}

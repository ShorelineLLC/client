package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;

import java.text.DecimalFormat;

public class CoordsHudModule extends DynamicHudModule
{
    Config<Boolean> netherConfig = new BooleanConfig.Builder("Nether")
            .setDescription("Show nether coordinates")
            .setDefaultValue(true).build();

    private final DecimalFormat decimal = new DecimalFormat("0.0");

    public CoordsHudModule()
    {
        super("Coords", "Displays the player coordinates", 200, 400);
    }

    @Override
    public void onEnable()
    {
        getHudEntries().add(new DynamicEntry(this, this::getCoordsText, () -> true));
    }

    public String getCoordsText()
    {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        boolean nether = mc.world.getRegistryKey() == World.NETHER;
        return String.format("XYZ " + Formatting.WHITE + "%s, %s, %s " + (netherConfig.getValue() ? Formatting.GRAY + "[" + Formatting.WHITE + "%s, %s" + Formatting.GRAY + "]" : ""),
                decimal.format(x),
                decimal.format(y),
                decimal.format(z),
                nether ? decimal.format(x * 8) : decimal.format(x / 8),
                nether ? decimal.format(z * 8) : decimal.format(z / 8));
    }
}

package net.shoreline.client.impl.module.combat;


import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class OffhandGappleModule extends Toggleable
{
    Config<Boolean> swordConfig = new BooleanConfig.Builder("Swords")
            .setDescription("Allows gapples in offhand when holding a sword")
            .setDefaultValue(true).build();
    Config<Boolean> toolsConfig = new BooleanConfig.Builder("Tools")
            .setDescription("Allows gapples in offhand when holding a tool")
            .setDefaultValue(true).build();
    Config<Boolean> totemConfig = new BooleanConfig.Builder("Totems")
            .setDescription("Allows gapples in offhand when holding a totem")
            .setDefaultValue(true).build();

    public OffhandGappleModule()
    {
        super("OffhandGapple", "Swaps golden apples into your offhand", GuiCategory.COMBAT);
    }


}

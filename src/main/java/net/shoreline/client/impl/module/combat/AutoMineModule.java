package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AutoMineModule extends Toggleable
{
    public static AutoMineModule INSTANCE;

    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(10.0f).setDefaultValue(6.0f).setFormat("m")
            .setDescription("The max range to target players").build();
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only attempts to mine visible faces")
            .setDefaultValue(true).build();
    Config<Boolean> antiCrawl = new BooleanConfig.Builder("AntiCrawl")
            .setDescription("Attempts to mine blocks to prevent player crawl")
            .setDefaultValue(true).build();
    Config<Boolean> feetConfig = new BooleanConfig.Builder("Feet")
            .setDescription("Mines out target feet blocks")
            .setDefaultValue(true).build();
    Config<Boolean> headConfig = new BooleanConfig.Builder("Head")
            .setDescription("Mines out target head blocks")
            .setDefaultValue(false).build();
    Config<Boolean> aboveHead = new BooleanConfig.Builder("AboveHead")
            .setDescription("Mines out above target head blocks")
            .setDefaultValue(false).build();
    Config<Boolean> avoidSelf = new BooleanConfig.Builder("AvoidSelf")
            .setDescription("Avoids mining out blocks we are near")
            .setDefaultValue(false).build();
    Config<Void> targetingConfig = new ConfigGroup.Builder("Targeting")
            .addAll(feetConfig, headConfig, aboveHead, avoidSelf).build();

    public AutoMineModule()
    {
        super("AutoMine", "Mines blocks around enemies", GuiCategory.COMBAT);
        INSTANCE = this;
    }


}

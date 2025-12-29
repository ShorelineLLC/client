package net.shoreline.client.impl.module.render;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

@Getter
public class ModelsModule extends Toggleable
{
    public static ModelsModule INSTANCE;

    // Config<Void> playersConfig = new ConfigGroup.Builder("Players").build();

    Config<Float> crystalScale = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.1f).setMax(1.5f).setDefaultValue(1.0f)
            .setDescription("The scale of the crystal model").build();
    Config<Float> crystalSpin = new NumberConfig.Builder<Float>("Spin")
            .setMin(0.0f).setMax(10.0f).setDefaultValue(1.0f)
            .setDescription("The spin speed of the crystal model").build();
    Config<Boolean> crystalBounce = new BooleanConfig.Builder("Bounce")
            .setDescription("Render the crystal bounce animation")
            .setDefaultValue(true).build();
    Config<Void> crystalsConfig = new ConfigGroup.Builder("Crystals")
            .addAll(crystalScale, crystalSpin, crystalBounce).build();

    public ModelsModule()
    {
        super("Models", "Modify entity models", GuiCategory.RENDER);
        INSTANCE = this;
    }
}

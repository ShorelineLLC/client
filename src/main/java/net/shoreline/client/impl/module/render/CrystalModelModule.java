package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class CrystalModelModule extends Toggleable
{
    Config<Float> crystalScale = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.1f).setMax(1.5f).setDefaultValue(1.0f)
            .setDescription("The scale of the crystal model").build();
    Config<Float> crystalSpin = new NumberConfig.Builder<Float>("Spin")
            .setMin(0.0f).setMax(10.0f).setDefaultValue(1.0f)
            .setDescription("The spin speed of the crystal model").build();
    Config<Boolean> crystalBounce = new BooleanConfig.Builder("Bounce")
            .setDescription("Render the crystal bounce animation")
            .setDefaultValue(true).build();

    public CrystalModelModule()
    {
        super("CrystalModel", "Modify the crystal model", GuiCategory.RENDER);
    }
}

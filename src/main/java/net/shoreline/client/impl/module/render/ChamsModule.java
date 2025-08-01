package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ChamsModule extends Toggleable
{
    private static ChamsModule INSTANCE;
    public Config<Float> scale = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f).build();
    public Config<Float> speed = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(0.5f).build();

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", GuiCategory.RENDER);
        INSTANCE = this;
    }

    public static ChamsModule getInstance()
    {
        return INSTANCE;
    }

    public float getSpeed()
    {
        return speed.getValue();
    }

    public float getScale()
    {
        return scale.getValue();
    }
}

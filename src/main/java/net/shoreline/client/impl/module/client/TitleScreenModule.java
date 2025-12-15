package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowManager;

@Getter
public class TitleScreenModule extends Toggleable
{
    public static TitleScreenModule INSTANCE;
    Config<Integer> snowflakes = new NumberConfig.Builder<Integer>("Snowflakes")
            .setMin(0).setMax(2000).setDefaultValue(750).build();

    public TitleScreenModule()
    {
        super("TitleScreen", "Enables shorelines custom title screen", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    public SnowManager getManager()
    {
        return new SnowManager(snowflakes.getValue());
    }
}

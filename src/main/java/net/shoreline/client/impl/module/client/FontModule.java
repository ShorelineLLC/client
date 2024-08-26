package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.init.Fonts;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class FontModule extends ToggleModule
{
    private static FontModule INSTANCE;

    Config<Integer> sizeConfig = register(new NumberConfig<>("Size", "The font size", 5, 9, 12));
    // Config<Boolean> shadowConfig = register(new BooleanConfig("Shadow", "Renders text with a shadow background", true));

    /**
     *
     */
    public FontModule()
    {
        super("Font", "Changes the client text to custom font rendering", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static FontModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE && Fonts.FONT_SIZE != sizeConfig.getValue())
        {
            Fonts.setSize(sizeConfig.getValue());
        }
    }

    /**
     * @return
     */
    public boolean getShadow()
    {
        return false;
    }
}

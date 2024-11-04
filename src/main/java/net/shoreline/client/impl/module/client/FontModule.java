package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.gui.hud.ChatTextRenderEvent;
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

    Config<Boolean> antiAliasConfig = register(new BooleanConfig("AntiAlias", "Applies antialiasing to font", true));
    Config<Boolean> fractionalMetrics = register(new BooleanConfig("FractionalMetrics", "Applies fractional metrics to font", false));
    Config<Integer> sizeConfig = register(new NumberConfig<>("Size", "The font size", 5, 9, 12));
    Config<Boolean> shadowConfig = register(new BooleanConfig("VanillaShadow", "Renders vanilla text with a shadow background", true, () -> !isEnabled()));
    Config<Boolean> chatFontConfig = register(new BooleanConfig("ChatFont", "Uses custom font for chat text", false));

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

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (!Fonts.isInitialized())
        {
            return;
        }
        if (event.getStage() == StageEvent.EventStage.POST
                && (event.getConfig() == antiAliasConfig || event.getConfig() == fractionalMetrics))
        {
            Fonts.closeFonts();
        }
    }

    @EventListener
    public void onChatTextRender(ChatTextRenderEvent event)
    {
        if (chatFontConfig.getValue())
        {
            event.cancel();
        }
    }

    public boolean getAntiAlias()
    {
        return antiAliasConfig.getValue();
    }

    public boolean getFractionalMetrics()
    {
        return fractionalMetrics.getValue();
    }

    /**
     * @return
     */
    public boolean getShadow()
    {
        return shadowConfig.getValue();
    }
}

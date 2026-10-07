package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.font.Fonts;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
public class FontModule extends Toggleable
{
    public static FontModule INSTANCE;

    Config<Fonts> fontsConfig = new EnumConfig.Builder<Fonts>("Font")
            .setValues(Fonts.values())
            .setDescription("The font for the client")
            .setDefaultValue(Fonts.VERDANA).build();
    Config<Boolean> antiAlias = new BooleanConfig.Builder("AntiAlias")
            .setDescription("Applies AA texturing on font")
            .setDefaultValue(true).build();
    Config<Boolean> fractionalMetrics = new BooleanConfig.Builder("FractionalMetrics")
            .setDescription("Applies fractional metrics on font")
            .setDefaultValue(false).build();
    Config<Boolean> overrideChat = new BooleanConfig.Builder("OverrideChat")
            .setDescription("Overrides the font in chat")
            .setDefaultValue(false).build();

    public FontModule()
    {
        super("Font", "Client custom fonts", GuiCategory.CLIENT);
        INSTANCE = this;

        fontsConfig.addObserver(v -> setFont(v.getName()));
        antiAlias.addObserver(v -> FontManager.close());
        fractionalMetrics.addObserver(v -> FontManager.close());
    }

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        setFont(fontsConfig.getValue().getName());
    }

    public void setFont(String fontName)
    {
        FontManager.setFont(FontManager.fromSystem(fontName));
    }
}

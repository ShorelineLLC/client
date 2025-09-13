package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

@Getter
public class FontModule extends Toggleable
{
    public static FontModule INSTANCE;

    Config<Boolean> overrideChat = new BooleanConfig.Builder("OverrideChat")
            .setDescription("Overrides the font in chat")
            .setDefaultValue(false).build();

    public FontModule()
    {
        super("Font", "Client custom fonts", GuiCategory.CLIENT);
        INSTANCE = this;
    }
}

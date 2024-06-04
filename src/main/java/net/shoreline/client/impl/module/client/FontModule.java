package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

/**
 * @author linus
 * @since 1.0
 */
public class FontModule extends ToggleModule
{
    private static FontModule INSTANCE;

    //
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

    /**
     * @return
     */
    public boolean getShadow()
    {
        return false;
    }
}

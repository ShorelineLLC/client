package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class CriticalsModule extends Toggleable
{
    Config<CritMode> modeConfig = new EnumConfig.Builder<CritMode>("Mode")
            .setValues(CritMode.values()).setDescription("The critical attack packet mode")
            .setDefaultValue(CritMode.PACKET).build();

    public CriticalsModule()
    {
        super("Criticals", "Always land critical hits", GuiCategory.COMBAT);
    }

    public enum CritMode
    {
        PACKET,
        PACKET_STRICT,
        GRIM_V3
    }
}

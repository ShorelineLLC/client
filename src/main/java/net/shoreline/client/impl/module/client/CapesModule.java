package net.shoreline.client.impl.module.client;

import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.CapesEvent;
import net.shoreline.client.impl.manager.client.cape.CapeType;
import net.shoreline.eventbus.annotation.EventListener;

public final class CapesModule extends ToggleModule
{
    Config<Capes> clientConfig = register(new EnumConfig<>("Client", "Shows client capes", Capes.OFF, Capes.values()));
    Config<Boolean> optifineConfig = register(new BooleanConfig("Optifine", "Shows optifine capes", true));

    public CapesModule()
    {
        super("Capes", "Shows player capes", ModuleCategory.CLIENT);
        enable();
    }

    @EventListener
    public void onCapes(CapesEvent event)
    {
        if (!optifineConfig.getValue() || clientConfig.getValue() == Capes.OFF)
        {
            return;
        }
        event.cancel();
        switch (clientConfig.getValue())
        {
            case WHITE -> event.setTexture(new Identifier("shoreline", "cape/cape_white.png"));
            case BLACK -> event.setTexture(new Identifier("shoreline", "cape/cape_black.png"));
        }
    }

    public enum Capes
    {
        WHITE,
        BLACK,
        OFF
    }
}

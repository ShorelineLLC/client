package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.CapesEvent;
import net.shoreline.client.impl.manager.client.cape.CapeType;

/**
 * @author xgraza
 * @since 1.0
 */
public final class CapesModule extends ToggleModule
{
    Config<Boolean> optifineConfig = register(new BooleanConfig("Optifine", "If to show optifine capes", true));

    public CapesModule()
    {
        super("Capes", "Shows player capes", ModuleCategory.CLIENT);
        enable();
    }

    @EventListener
    public void onCapes(CapesEvent event) {
        event.cancel();
        if (optifineConfig.getValue()) {
            event.setCapeType(CapeType.OPTIFINE);
        }
    }
}

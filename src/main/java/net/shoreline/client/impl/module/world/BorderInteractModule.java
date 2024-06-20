package net.shoreline.client.impl.module.world;

import net.minecraft.item.BlockItem;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.InteractBorderEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class BorderInteractModule extends ToggleModule
{
    Config<Boolean> blocksConfig = register(new BooleanConfig("Blocks", "Allows you to place blocks on the border", false));

    public BorderInteractModule()
    {
        super("BorderInteract", "Allows you to interact with the world border", ModuleCategory.WORLD);
    }

    @EventListener
    public void onInteractBorder(InteractBorderEvent event)
    {
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem && !blocksConfig.getValue())
        {
            return;
        }
        event.cancel();
    }
}

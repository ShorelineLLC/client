package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.render.CameraClipEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class ViewClipModule extends ToggleModule
{

    Config<Float> distanceConfig = register(new NumberConfig<>("Distance", "The third-person camera clip distance", 1.0f, 3.5f, 20.0f));

    public ViewClipModule()
    {
        super("ViewClip", "Clips your third-person camera through blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onCameraClip(CameraClipEvent event)
    {
        event.cancel();
        event.setDistance(distanceConfig.getValue());
    }
}

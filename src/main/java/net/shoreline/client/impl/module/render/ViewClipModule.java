package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.CameraClipEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class ViewClipModule extends Toggleable
{
    Config<Float> distanceConfig = new NumberConfig.Builder<Float>("Distance")
            .setMin(1.0f).setMax(30.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The camera clip distance").build();

    public ViewClipModule()
    {
        super("ViewClip", new String[] {"CameraClip"},
                "Clips the third-person camera through blocks", GuiCategory.RENDER);
    }

    @EventListener
    public void onCameraClip(CameraClipEvent event)
    {
        event.cancel();
        event.setDistance(distanceConfig.getValue());
    }
}

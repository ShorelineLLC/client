package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.macro.HoldKeybind;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.option.FovEvent;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends ListeningToggleable
{
    public ZoomModule()
    {
        super("Zoom", "Zooms in the camera", GuiCategory.RENDER);
        setKeybind(new HoldKeybind(GLFW.GLFW_KEY_UNKNOWN, this));
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (mc.currentScreen != null || mc.options == null)
        {
            return;
        }

        if (isEnabled())
        {
            mc.options.smoothCameraEnabled = true;
            // mc.options.hudHidden = true;
        }
        else
        {
            mc.options.smoothCameraEnabled = false;
            // mc.options.hudHidden = false;
        }
    }

    @EventListener
    public void onFov(FovEvent event)
    {
        if (isEnabled())
        {
            event.cancel();
            event.setFov(30);
        }
    }
}

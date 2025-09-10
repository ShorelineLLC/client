package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.macro.HoldKeybind;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends ListeningToggleable
{
    private int prevFov = 100;

    public ZoomModule()
    {
        super("Zoom", "Zooms in the camera", GuiCategory.RENDER);
        setKeybind(new HoldKeybind(GLFW.GLFW_KEY_C, this));
    }

    @Override
    public void onEnable()
    {
        if (mc.options == null)
        {
            return;
        }

        prevFov = mc.options.getFov().getValue();

        if (mc.currentScreen != null)
        {
            return;
        }

        mc.options.smoothCameraEnabled = true;
        mc.options.getFov().setValue(30);
    }

    @Override
    public void onDisable()
    {
        if (mc.options == null)
        {
            return;
        }

        mc.options.smoothCameraEnabled = false;
        mc.options.getFov().setValue(prevFov);
    }
}

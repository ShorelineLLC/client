package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.event.InputEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends ListeningToggleable
{
    private boolean flag;
    private boolean flag1 = true;
    private int defaultFov = 100;

    private boolean isPressed;

    public ZoomModule()
    {
        super("Zoom", "Zooms in the camera", GuiCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        resetKeybind();
    }

    @EventListener
    public void onKey(InputEvent.Keyboard event)
    {
        if (event.getAction() != GLFW.GLFW_REPEAT && event.getKey() == getKeybindMacro().getKeycode())
        {
            isPressed = event.getAction() == GLFW.GLFW_PRESS;
        }
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (isEnabled() && mc.currentScreen == null)
        {
            if (isPressed)
            {
                if (flag1)
                {
                    defaultFov = mc.options.getFov().getValue();
                    flag1 = false;
                }
                mc.options.smoothCameraEnabled = true;
                mc.options.hudHidden = true;
                mc.options.getFov().setValue(30);
                flag = true;
            }
            else if (flag)
            {
                mc.options.smoothCameraEnabled = false;
                mc.options.hudHidden = false;
                mc.options.getFov().setValue(defaultFov);
                flag = false;
                flag1 = true;
            }
        }
    }
}

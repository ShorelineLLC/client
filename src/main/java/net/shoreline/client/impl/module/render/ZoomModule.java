package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.MacroConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.keyboard.KeyboardInputEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends ToggleModule
{
    Config<Integer> zoomConfig = register(new NumberConfig<>("Zoom", "The zoom value", 10, 30, 50));
    Config<Boolean> smoothCameraConfig = register(new BooleanConfig("SmoothCamera", "Adds motion reduction to the camera", true));
    Config<Macro> zoomKeyConfig = register(new MacroConfig("ZoomKey", "The zoom key bind", new Macro(getId() + "-zoomkey", GLFW.GLFW_KEY_C, () -> {})));

    private boolean flag;
    private boolean flag1 = true;
    private boolean isPressed;
    private int defaultFov = 100;

    public ZoomModule()
    {
        super("Zoom", "Zooms in the camera perspective", ModuleCategory.RENDER);
    }

    @EventListener
    public void onKey(KeyboardInputEvent event)
    {
        if (event.getAction() != GLFW.GLFW_REPEAT && event.getKeycode() == zoomKeyConfig.getValue().getKeycode())
        {
            isPressed = event.getAction() == GLFW.GLFW_PRESS;
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE && mc.currentScreen == null)
        {
            if (isPressed)
            {
                if (flag1)
                {
                    defaultFov = mc.options.getFov().getValue();
                    flag1 = false;
                }
                mc.options.smoothCameraEnabled = smoothCameraConfig.getValue();
                mc.options.hudHidden = true;
                mc.options.getFov().setValue(zoomConfig.getValue());
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

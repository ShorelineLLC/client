package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.MacroConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.keyboard.KeyboardInputEvent;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends ToggleModule {

    Config<Macro> zoomKeyConfig = new MacroConfig("ZoomKey", "The zoom key bind", new Macro(getId(), GLFW.GLFW_KEY_C, null));

    private boolean flag;
    private boolean flag1 = true;
    private boolean isPressed;
    public static int defaultFov = 100;
    public static int targetFov = 30;

    public ZoomModule() {
        super("Zoom", "Zooms in the camera perspective", ModuleCategory.RENDER);
    }

    @EventListener
    public void onKey(KeyboardInputEvent event) {
        if (event.getAction() != GLFW.GLFW_REPEAT && event.getKeycode() == zoomKeyConfig.getValue().getKeycode()) {
            isPressed = event.getAction() == GLFW.GLFW_PRESS;
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && mc.currentScreen == null) {
            if (isPressed) {
                if (flag1) {
                    defaultFov = mc.options.getFov().getValue();
                    flag1 = false;
                }
                mc.options.smoothCameraEnabled = true;
                mc.options.getFov().setValue(targetFov);
                flag = true;
            } else if (flag) {
                mc.options.smoothCameraEnabled = false;
                mc.options.getFov().setValue(defaultFov);
                flag = false;
                flag1 = true;
            }
        }
    }
}

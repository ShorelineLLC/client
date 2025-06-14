package net.shoreline.client.api.macro;

import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.InputEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CopyOnWriteArrayList;

public class MacroManager extends GenericFeature
{
    private final CopyOnWriteArrayList<Macro> macros = new CopyOnWriteArrayList<>();

    public MacroManager()
    {
        super("Macros");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onKeyboardInput(InputEvent.Keyboard event)
    {
        if (checkNull() || mc.currentScreen != null || event.getAction() != GLFW.GLFW_PRESS
                || event.getKey() == GLFW.GLFW_KEY_UNKNOWN)
        {
            return;
        }

        for (Macro macro : macros)
        {
            if (macro.getKeycode() <= GLFW.GLFW_KEY_LAST && event.getKey() == macro.getKeycode())
            {
                macro.onKeyPress();
            }
        }
    }

    public void register(Macro macro)
    {
        macros.addIfAbsent(macro);
    }

    public void unregister(Macro macro)
    {
        macros.remove(macro);
    }
}

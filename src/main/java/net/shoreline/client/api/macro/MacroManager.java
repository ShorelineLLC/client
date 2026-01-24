package net.shoreline.client.api.macro;

import lombok.Getter;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.InputEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CopyOnWriteArrayList;

public class MacroManager extends GenericFeature
{
    @Getter
    private final CopyOnWriteArrayList<Macro> macros = new CopyOnWriteArrayList<>();

    public MacroManager()
    {
        super("Macros");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onKeyboardInput(InputEvent.Keyboard event)
    {
        if (checkNull() || mc.currentScreen != null)
        {
            return;
        }

        for (Macro macro : macros)
        {
            onInput(event.getKey(), event.getAction(), macro, false);
        }
    }

    @EventListener
    public void onMouseInput(InputEvent.Mouse event)
    {
        if (checkNull() || mc.currentScreen != null)
        {
            return;
        }

        for (Macro macro : macros)
        {
            onInput(event.getButton(), event.getAction(), macro, true);
        }
    }

    public void onInput(int eventKey, int action, Macro macro, boolean mouse)
    {
        int key = macro.getKeycode();
        if (mouse)
        {
            key -= GLFW.GLFW_KEY_LAST;
        }

        if (macro instanceof HoldKeybind hold)
        {
            if (eventKey == key
                    && (action == GLFW.GLFW_PRESS
                    || action == GLFW.GLFW_REPEAT))
            {
                hold.onKeyPress();
            } else if (action == GLFW.GLFW_RELEASE)
            {
                hold.onKeyRelease();
            }
        }
        else
        {
            if (key == GLFW.GLFW_KEY_UNKNOWN || action != GLFW.GLFW_PRESS)
            {
                return;
            }

            if (eventKey == key)
            {
                macro.onKeyPress();
            }
        }
    }

    public Macro getMacro(String string)
    {
        for (Macro macro : macros)
        {
            if (macro instanceof ModuleKeybind)
            {
                continue;
            }

            if (macro.getName().equalsIgnoreCase(string))
            {
                return macro;
            }
        }

        return null;
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

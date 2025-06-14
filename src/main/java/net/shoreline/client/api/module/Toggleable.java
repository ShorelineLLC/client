package net.shoreline.client.api.module;

import com.google.gson.JsonObject;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.impl.Managers;
import net.shoreline.eventbus.EventBus;
import org.lwjgl.glfw.GLFW;

public class Toggleable extends Module
{
    final Config<Boolean> enabled = new BooleanConfig.Builder("Enabled", "Module enabled state")
            .setNameAliases("Toggled").setDefaultValue(false).build();

    final Config<Macro> keybind = new MacroConfig.Builder("Bind", "The module keybind")
            .setNameAliases("Keybind").setDefaultValue(new ModuleKeybind(GLFW.GLFW_KEY_UNKNOWN, this)).build();

    public Toggleable(final String name,
                      final String description,
                      final GuiCategory category)
    {
        super(name, description, category);
    }

    public Toggleable(final String name,
                      final String[] nameAliases,
                      final String description,
                      final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public void enable()
    {
        EventBus.INSTANCE.subscribe(this);
        onEnable();
    }

    public void disable()
    {
        onDisable();
        EventBus.INSTANCE.unsubscribe(this);
    }

    public void toggle()
    {
        if (isEnabled())
        {
            disable();
        } else
        {
            enable();
        }
    }

    protected void onEnable() {}

    protected void onDisable() {}

    @Override
    public JsonObject toJson()
    {
        return null;
    }

    @Override
    public void fromJson(JsonObject jsonObject)
    {

    }

    public boolean isEnabled()
    {
        return enabled.getValue();
    }

    public void setKeybind(int keycode)
    {
        Managers.MACROS.unregister(getKeybind());
        ModuleKeybind keybind1 = new ModuleKeybind(keycode, this);
        keybind.setValue(keybind1);
        Managers.MACROS.register(keybind1);
    }

    public Macro getKeybind()
    {
        return keybind.getValue();
    }
}

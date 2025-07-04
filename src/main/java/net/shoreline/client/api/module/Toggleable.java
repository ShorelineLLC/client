package net.shoreline.client.api.module;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.impl.Managers;
import net.shoreline.eventbus.EventBus;
import org.lwjgl.glfw.GLFW;

@Getter
public class Toggleable extends Module
{
    protected final Config<Boolean> enabled = new BooleanConfig.Builder("Enabled")
            .setDescription("Module enabled state")
            .setNameAliases("Toggled")
            .setDefaultValue(false).build();
    protected final Config<Macro> keybind = new MacroConfig.Builder("Keybind")
            .setDescription("The module keybind")
            .setNameAliases("Bind")
            .setDefaultValue(new ModuleKeybind(GLFW.GLFW_KEY_UNKNOWN, this)).build();

    public Toggleable(final String name,
                      final String description,
                      final GuiCategory category)
    {
        super(name, description, category);
        registerConfig(keybind);
    }

    public Toggleable(final String name,
                      final String[] nameAliases,
                      final String description,
                      final GuiCategory category)
    {
        super(name, nameAliases, description, category);
        registerConfig(keybind);
    }

    public void enable()
    {
        EventBus.INSTANCE.subscribe(this);
        enabled.setValue(true);
        onEnable();
    }

    public void disable()
    {
        onDisable();
        enabled.setValue(false);
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

    public boolean isEnabled()
    {
        return enabled.getValue();
    }

    public void setKeybind(int keycode)
    {
        Managers.MACROS.unregister(getKeybindMacro());
        ModuleKeybind keybind1 = new ModuleKeybind(keycode, this);
        keybind.setValue(keybind1);
        Managers.MACROS.register(keybind1);
    }

    public Macro getKeybindMacro()
    {
        return keybind.getValue();
    }
}

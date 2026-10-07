package net.shoreline.client.api.module;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.util.text.Formatted;
import net.shoreline.eventbus.EventBus;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

@Getter
public class Toggleable extends Module implements Formatted
{
    protected final Config<Boolean> enabled = new BooleanConfig.Builder("Enabled")
            .setDescription("Module enabled state")
            .setNameAliases("Toggled")
            .setDefaultValue(false).build();
    protected final Config<Macro> keybind = new MacroConfig.Builder("Keybind")
            .setDescription("The module keybind")
            .setNameAliases("Bind")
            .setDefaultValue(new ModuleKeybind(GLFW.GLFW_KEY_UNKNOWN, this)).build();
    protected final Config<Boolean> hidden = new BooleanConfig.Builder("Hidden")
            .setDescription("Module hidden state")
            .setNameAliases("Drawn")
            .setVisible(() -> false)
            .setDefaultValue(false).build();
    protected final Config<Boolean> notify = new BooleanConfig.Builder("Notify")
            .setDescription("Notifies in chat on toggle")
            .setVisible(() -> false)
            .setDefaultValue(false).build();

    public Toggleable(final String name,
                      final String description,
                      final GuiCategory category)
    {
        super(name, description, category);
        registerConfigs(keybind, hidden, notify);
    }

    public Toggleable(final String name,
                      final String[] nameAliases,
                      final String description,
                      final GuiCategory category)
    {
        super(name, nameAliases, description, category);
        registerConfigs(keybind, hidden, notify);
    }

    public void enable()
    {
        EventBus.INSTANCE.subscribe(this);
        EventBus.INSTANCE.dispatch(new ModuleToggleEvent(this, true));
        enabled.setValue(true);
        onEnable();
        onToggle();
    }

    public void disable()
    {
        onDisable();
        onToggle();
        enabled.setValue(false);
        EventBus.INSTANCE.dispatch(new ModuleToggleEvent(this, false));
        EventBus.INSTANCE.unsubscribe(this);
    }

    public boolean toggle()
    {
        if (isEnabled())
        {
            disable();
        } else
        {
            enable();
        }
        return isEnabled();
    }

    protected void onEnable() {}

    protected void onDisable() {}

    protected void onToggle() {}

    public String getModuleData()
    {
        return null;
    }

    public boolean isEnabled()
    {
        return enabled.getValue();
    }

    public void resetKeybind()
    {
        Managers.MACROS.unregister(getKeybindMacro());
    }

    public void setKeybind(ModuleKeybind bind)
    {
        resetKeybind();
        keybind.setValue(bind);
        Managers.MACROS.register(bind);
    }

    public Macro getKeybindMacro()
    {
        return keybind.getValue();
    }

    public void setHidden(boolean hidden)
    {
        this.hidden.setValue(hidden);
    }

    public boolean isHidden()
    {
        return hidden.getValue();
    }

    public void setNotify(boolean notify)
    {
        this.notify.setValue(notify);
    }

    public boolean shouldNotify()
    {
        return notify.getValue();
    }

}

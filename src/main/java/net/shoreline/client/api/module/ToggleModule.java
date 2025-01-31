package net.shoreline.client.api.module;

import net.shoreline.client.api.Hideable;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.MacroConfig;
import net.shoreline.client.api.config.setting.ToggleConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import org.lwjgl.glfw.GLFW;

/**
 * {@link Module} implementation with enabled state and keybinding. The
 * enabled state dictates when the module is running and subscribed to the
 * EventBus. The keybinding is used to {@link #enable()} and
 * {@link #disable()} the module.
 *
 * <p>The user cannot directly interact with the {@link #enabledConfig}. This
 * is the only config which cannot be interacted with through the configuration
 * menu in the ClickGui. Instead, the user can {@link #toggle()} the module
 * to change the enabled state.</p>
 *
 * @author linus
 * @see Macro
 * @see ToggleConfig
 * @since 1.0
 */
public class ToggleModule extends Module implements Hideable
{
    //
    private final Animation animation = new Animation(false, 300, Easing.CUBIC_IN_OUT);

    // Config representing the module enabled state. Cannot interact with
    // this configuration unless using #toggle() #enable() or #disable().
    Config<Boolean> enabledConfig = register(new ToggleConfig("Enabled", "The module enabled state", false));
    // Config for keybinding implementation. Module keybind is used to
    // interact with the #enabledConfig.
    Config<Macro> keybindingConfig = register(new MacroConfig("Keybind", "The keybind to toggle the module",
            new Macro(getId(), GLFW.GLFW_KEY_UNKNOWN, () -> toggle())));
    // Arraylist rendering info
    Config<Boolean> hiddenConfig = register(new BooleanConfig("Hidden", "The hidden state of the module in the arraylist", false));
    // Notifies in chat
    Config<Boolean> notifyConfig = register(new BooleanConfig("Notify", "Notifies you when the module is toggled in chat", false, () -> false));

    /**
     * @param name     The module unique identifier
     * @param desc     The module description
     * @param category The module category
     */
    public ToggleModule(String name, String desc, ModuleCategory category)
    {
        super(name, desc, category);
        // Toggle settings
        register(keybindingConfig, enabledConfig, hiddenConfig);
    }

    /**
     * @param name     The module unique identifier
     * @param desc     The module description
     * @param category The module category
     * @param keycode  The module default keybind
     */
    public ToggleModule(String name, String desc, ModuleCategory category,
                        Integer keycode)
    {
        this(name, desc, category);
        keybind(keycode);
    }

    /**
     * @return
     */
    @Override
    public boolean isHidden()
    {
        return hiddenConfig.getValue();
    }

    /**
     * @param hidden
     */
    @Override
    public void setHidden(boolean hidden)
    {
        hiddenConfig.setValue(hidden);
    }

    /**
     * Toggles the module {@link #enabledConfig} state (i.e. If the module is
     * <tt>enabled</tt>, the module enabled state will now be <tt>disabled</tt>
     * and vice versa).
     *
     * @see #enable()
     * @see #disable()
     */
    public void toggle()
    {
        if (isEnabled())
        {
            disable();
        }
        else
        {
            enable();
        }
    }

    /**
     * Sets the module {@link #enabledConfig} state to <tt>true</tt>. Runs
     * the {@link #onEnable()} callback.
     *
     * @see #onEnable()
     * @see ToggleConfig#setValue(Boolean)
     */
    public void enable()
    {
        enabledConfig.setValue(true);
        onEnable();
        if (notifyConfig.getValue() && mc.world != null)
        {
            ChatUtil.clientSendMessage("§7" + getName() + "§f toggled §son", hashCode());
        }
    }

    /**
     * Sets the module {@link #enabledConfig} state to <tt>false</tt>. Runs
     * the {@link #onDisable()} callback.
     *
     * @see #onDisable()
     * @see ToggleConfig#setValue(Boolean)
     */
    public void disable()
    {
        enabledConfig.setValue(false);
        onDisable();
        if (notifyConfig.getValue() && mc.world != null)
        {
            ChatUtil.clientSendMessage("§7" + getName() + "§f toggled §coff", hashCode());
        }
    }

    /**
     * Runs callback after {@link #enable()}. Part of the module
     * implementation specifications.
     *
     * @see #enable()
     */
    protected void onEnable()
    {

    }

    /**
     * Runs callback after {@link #disable()}. Part of the module
     * implementation specifications.
     *
     * @see #disable()
     */
    protected void onDisable()
    {

    }

    /**
     * Sets the module keybinding to the param {@link GLFW} keycode. The
     * config {@link Macro#runMacro()} will invoke {@link #toggle()} when
     * keybind is pressed.
     *
     * @param keycode The keybind
     * @see Macro
     * @see #keybindingConfig
     */
    public void keybind(int keycode)
    {
        keybindingConfig.setContainer(this);
        ((MacroConfig) keybindingConfig).setValue(keycode);
    }

    /**
     * Returns <tt>true</tt> if the module is currently enabled and running.
     * Wrapper method for {@link ToggleConfig#getValue()}.
     *
     * @return <tt>true</tt> if the module is enabled
     * @see #enabledConfig
     */
    public boolean isEnabled()
    {
        return enabledConfig.getValue();
    }

    /**
     * @return
     */
    public Macro getKeybinding()
    {
        return keybindingConfig.getValue();
    }

    public void setNotify(boolean notify)
    {
        notifyConfig.setValue(notify);
    }

    public boolean getNotify()
    {
        return notifyConfig.getValue();
    }

    /**
     * @return
     */
    public Animation getAnimation()
    {
        return animation;
    }
}

package net.shoreline.client.api.module;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigContainer;
import net.shoreline.client.api.config.Serializable;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * General client feature that will appear in the ClickGui. Module have a
 * unique name which is also used as the module identifier. Modules are
 * grouped by {@link ModuleCategory}.
 *
 * <p>Modules are {@link Serializable} and hold {@link Config}
 * in a {@link ConfigContainer}. Modules can be configured in the ClickGui
 * or through the use of Commands in the chat.</p>
 *
 * @author linus
 * @see ToggleModule
 * @see ConcurrentModule
 * @since 1.0
 */
public class Module extends ConfigContainer implements Globals
{
    //
    public static final String MODULE_ID_FORMAT = "%s-module";
    // Concise module description, displayed in the ClickGui to help users
    // understand the functionality of the module.
    private final String desc;
    // Modules are grouped into categories for easy navigation in the
    // ClickGui. Modules with ModuleCategory.TEST category are not available
    // to the user.
    private final ModuleCategory category;

    /**
     * @param name     The unique module identifier
     * @param desc     The module description
     * @param category The module category
     */
    public Module(String name, String desc, ModuleCategory category)
    {
        super(name);
        this.desc = desc;
        this.category = category;
    }

    /**
     * @param message
     */
    protected void sendModuleMessage(String message)
    {
        ChatUtil.clientSendMessageRaw("§s[%s]§f %s", name, message);
    }

    /**
     * @param message
     * @param params
     */
    protected void sendModuleMessage(String message, Object... params)
    {
        sendModuleMessage(String.format(message, params));
    }

    /**
     * @param message
     */
    protected void sendModuleError(String message)
    {
        ChatUtil.clientSendMessageRaw("§s[%s]§c %s", name, message);
    }

    /**
     * @param message
     * @param params
     */
    protected void sendModuleError(String message, Object... params)
    {
        sendModuleError(String.format(message, params));
    }

    /**
     * @return
     */
    @Override
    public String getId()
    {
        return String.format(MODULE_ID_FORMAT, name.toLowerCase());
    }

    /**
     * @return
     */
    public String getDescription()
    {
        return desc;
    }

    /**
     * Returns the {@link ModuleCategory} of the module.
     *
     * @return The category of the module
     * @see ModuleCategory
     */
    public ModuleCategory getCategory()
    {
        return category;
    }

    /**
     * @return
     */
    public String getModuleData()
    {
        return "ARRAYLIST_INFO";
    }
}

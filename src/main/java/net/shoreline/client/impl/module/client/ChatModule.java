package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;

/**
 * @author linus
 * @since 1.0
 */
public class ChatModule extends ConcurrentModule
{
    public ChatModule()
    {
        super("Chat", "Manages the client chat", ModuleCategory.CLIENT);
    }
}

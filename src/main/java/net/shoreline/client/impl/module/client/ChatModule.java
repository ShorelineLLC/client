package net.shoreline.client.impl.module.client;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.render.animation.TimeAnimation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linus
 * @since 1.0
 */
public class ChatModule extends ConcurrentModule {
    //
    Config<Boolean> debugConfig = new BooleanConfig("ChatDebug", "Allows client debug messages to be printed in the chat", false);

    /**
     *
     */
    public ChatModule() {
        super("Chat", "Manages the client chat", ModuleCategory.CLIENT);
    }
}

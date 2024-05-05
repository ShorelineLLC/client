package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.render.animation.TimeAnimation;

import java.util.HashMap;
import java.util.Map;

//TODO: add easing when linus fixes enumconfig...
public class BetterChatModule extends ToggleModule
{
    Config<Boolean> animation = new BooleanConfig("Animation", "Animates the chat", false);
    Config<Integer> time = new NumberConfig<>("Time", "Time for the animation", 0, 200, 1000);

    public final Map<ChatHudLine, TimeAnimation> animationMap = new HashMap<>();

    public BetterChatModule()
    {
        super("BetterChat", "Modifications for the chat", ModuleCategory.MISCELLANEOUS);
    }

    public Config<Boolean> getAnimationConfig()
    {
        return animation;
    }

    public Config<Integer> getTimeConfig()
    {
        return time;
    }

    /*
    public Config<Easing> getEasingConfig()
    {
        return easing;
    }
    */

    public Easing getEasingConfig()
    {
        return Easing.LINEAR;
    }
}

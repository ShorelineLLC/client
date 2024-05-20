package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.*;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.util.FormattingUtils;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.render.animation.TimeAnimation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//TODO: add easing when linus fixes enumconfig...
public class BetterChatModule extends ToggleModule
{
    public static BetterChatModule INSTANCE;

    Config<Timestamp> timestampConfig = new EnumConfig<>("Timestamp", "Shows chat timestamps", Timestamp.OFF, Timestamp.values());
    Config<Boolean> animationConfig = new BooleanConfig("Animation", "Animates the chat", false);
    Config<Integer> timeConfig = new NumberConfig<>("Anim-Time", "Time for the animation", 0, 200, 1000, () -> false);
    Config<Boolean> noSignatureConfig = new BooleanConfig("NoSignatureIndicator", "Removes the message signature indicator", false);

    public final Map<ChatHudLine, TimeAnimation> animationMap = new HashMap<>();

    public BetterChatModule()
    {
        super("BetterChat", "Modifications for the chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onChatText(ChatMessageEvent event) {
        if (timestampConfig.getValue() != Timestamp.OFF) {
            String time = new SimpleDateFormat("k:mm").format(new Date());
            String text = switch (timestampConfig.getValue()) {
                case NORMAL -> "§8<§7" + time + "§8>§r ";
                case COLOR -> "§s<" + time + ">§r ";
                case OFF -> "";
            };
            event.cancel();
            event.setText(Text.of(text + FormattingUtils.toString(event.getText())));
        }
    }

    public Config<Boolean> getAnimationConfig()
    {
        return animationConfig;
    }

    public Config<Integer> getTimeConfig()
    {
        return timeConfig;
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

    public Config<Boolean> getNoSignatureConfig() {
        return noSignatureConfig;
    }

    public enum Timestamp {
        NORMAL,
        COLOR,
        OFF
    }
}

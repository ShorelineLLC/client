package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.hud.ChatTextEvent;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.render.animation.TimeAnimation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//TODO: add easing when linus fixes enumconfig...
public class BetterChatModule extends ToggleModule
{
    Config<Timestamp> timestampConfig = new EnumConfig<>("Timestamp", "Shows chat timestamps", Timestamp.OFF, Timestamp.values());
    Config<Boolean> animationConfig = new BooleanConfig("Animation", "Animates the chat", false);
    Config<Integer> timeConfig = new NumberConfig<>("Anim-Time", "Time for the animation", 0, 200, 1000, () -> animationConfig.getValue());

    public final Map<ChatHudLine, TimeAnimation> animationMap = new HashMap<>();

    public BetterChatModule()
    {
        super("BetterChat", "Modifications for the chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onChatText(ChatTextEvent event) {
        if (timestampConfig.getValue() != Timestamp.OFF) {
            String time = new SimpleDateFormat("k:mm").format(new Date());
            OrderedText text = switch (timestampConfig.getValue()) {
                case NORMAL -> OrderedText.concat(fromString("<", Style.EMPTY.withColor(Formatting.DARK_GRAY)), fromString(time, Style.EMPTY.withColor(Formatting.GRAY)), fromString("> ", Style.EMPTY.withColor(Formatting.DARK_GRAY)));
                case COLOR -> OrderedText.concat(fromString("<" + time + "> ", Style.EMPTY.withColor(Modules.COLORS.getRGB())));
                case OFF -> OrderedText.EMPTY;
            };
            event.cancel();
            event.setText(OrderedText.concat(text, event.getText()));
        }
    }

    private OrderedText fromString(String string, Style style) {
        return OrderedText.styledForwardsVisitedString(string, style);
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

    public enum Timestamp {
        NORMAL,
        COLOR,
        OFF
    }
}

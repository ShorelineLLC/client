package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.chat.ChatHistoryEvent;
import net.shoreline.client.impl.event.gui.chat.ChatLengthEvent;
import net.shoreline.client.impl.event.gui.hud.*;
import net.shoreline.client.util.FormattingUtil;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.render.animation.TimeAnimation;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//TODO: add easing when linus fixes enumconfig...
public class BetterChatModule extends ToggleModule
{
    Config<Boolean> chatFontConfig = register(new BooleanConfig("ChatFont", "Uses custom font for chat text", false));
    Config<Timestamp> timestampConfig = register(new EnumConfig<>("Timestamp", "Shows chat timestamps", Timestamp.OFF, Timestamp.values()));
    Config<AnimationMode> animationConfig = register(new EnumConfig<>("Animation", "Animates the chat", AnimationMode.OFF, AnimationMode.values()));
    Config<Integer> timeConfig = register(new NumberConfig<>("Anim-Time", "Time for the animation", 0, 200, 1000, () -> false));
    Config<Boolean> noSignatureConfig = register(new BooleanConfig("NoSignatureIndicator", "Removes the message signature indicator", false));
    Config<Boolean> infiniteConfig = register(new BooleanConfig("Infinite", "Makes chat length infinite", false));
    Config<Boolean> keepChatConfig = register(new BooleanConfig("KeepChat", "Maintains chat history", false));

    public final Map<ChatHudLine.Visible, TimeAnimation> animationMap = new HashMap<>();

    public BetterChatModule()
    {
        super("BetterChat", "Modifications for the chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onChatText(ChatMessageEvent event)
    {
        if (timestampConfig.getValue() != Timestamp.OFF)
        {
            String string = FormattingUtil.toString(event.getText());
            if (string.contains(ChatUtil.PREFIX))
            {
                return;
            }
            String time = new SimpleDateFormat("k:mm").format(new Date());
            String text = switch (timestampConfig.getValue())
            {
                case NORMAL -> "<" + time + ">§r ";
                case GRAY -> "§8<§7" + time + "§8>§r ";
                case COLOR -> "§s<" + time + ">§r ";
                case OFF -> "";
            };
            event.cancel();
            event.setText(Text.of(text + string));
        }
    }

    @EventListener
    public void onChatLine(ChatLineEvent event)
    {
        animationMap.put(event.getChatHudLine(), new TimeAnimation(false, event.getWidth(), 0,
                timeConfig.getValue(), Easing.LINEAR));
    }

    @EventListener
    public void onChatLineRender(RenderChatHudEvent event)
    {
        if (animationConfig.getValue() != AnimationMode.OFF)
        {
            TimeAnimation animation = null;
            if (event.getChatHudLine() != null)
            {
                if (animationMap.containsKey(event.getChatHudLine()))
                {
                    animation = animationMap.get(event.getChatHudLine());
                }
            }
            if (animation != null)
            {
                animation.setState(true);
                event.cancel();
                if (animationConfig.getValue() == AnimationMode.SLIDE)
                {
                    event.setAnimation(animation.getCurrent());
                }
                else
                {
                    event.setAnimation(animation.getFactor());
                }
                event.setSlide(animationConfig.getValue() == AnimationMode.SLIDE);
            }
        }
    }

    @EventListener
    public void onSignatureIndicator(SignatureIndicatorEvent event)
    {
        if (noSignatureConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onChatHistory(ChatHistoryEvent event)
    {
        if (keepChatConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onChatLength(ChatLengthEvent event)
    {
        if (infiniteConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onChatTextRender(ChatTextRenderEvent event)
    {
        if (chatFontConfig.getValue())
        {
            event.cancel();
        }
    }

    public enum Timestamp
    {
        NORMAL,
        GRAY,
        COLOR,
        OFF
    }

    public enum AnimationMode
    {
        SLIDE,
        FADE,
        OFF
    }
}

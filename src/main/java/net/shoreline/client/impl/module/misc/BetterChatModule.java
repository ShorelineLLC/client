package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.hud.MessageIndicatorEvent;
import net.shoreline.client.impl.event.gui.hud.RenderChatTextEvent;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BetterChatModule extends Toggleable
{
    Config<Boolean> animateConfig = new BooleanConfig.Builder("Animate")
            .setDescription("Animates the chat hud")
            .setDefaultValue(true).build();
    Config<Boolean> timestampConfig = new BooleanConfig.Builder("Timestamp")
            .setDescription("Adds a timestamp to all messages in chat")
            .setDefaultValue(false).build();
    Config<Boolean> noIndicator = new BooleanConfig.Builder("NoIndicator")
            .setDescription("Removes the message indicator")
            .setDefaultValue(false).build();

    private final ConcurrentMap<ChatHudLine.Visible, Animation> chatLineAnims = new ConcurrentHashMap<>();

    public BetterChatModule()
    {
        super("BetterChat", "Improves in-game chat", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onRenderChatText(RenderChatTextEvent event)
    {
        if (event.getChatLine() == null)
        {
            return;
        }

        if (animateConfig.getValue() && chatLineAnims.containsKey(event.getChatLine()))
        {
            Animation anim = chatLineAnims.get(event.getChatLine());

            anim.setState(true);
            if (anim.getFactor() == 1.0f)
            {
                return;
            }

            double factor = Easing.EXPO_IN_OUT.ease(anim.getFactor());
            int width = mc.textRenderer.getWidth(event.getText());
            int renderX = (int) (event.getX() - (width * (1.0f - factor)));

            event.cancel();
            event.getContext().drawTextWithShadow(mc.textRenderer, event.getText(),
                    renderX, event.getY(), ColorUtil.withTransparency(Colors.WHITE, (float) factor));
        }
    }

    @EventListener
    public void onChatMessage(ChatMessageEvent event)
    {
        String string = event.getText().getString();
        if (string.contains(RAW_PREFIX) || string.contains(ERROR_PREFIX) || string.contains(SUCCESS_PREFIX))
        {
            return;
        }

        MutableText chatPrefix = Text.empty();
        if (timestampConfig.getValue())
        {
            String time = new SimpleDateFormat("k:mm").format(new Date());
            chatPrefix = Text.literal(ClientFormatting.CLIENT + "<" + time + "> ");
        }

        event.cancel();
        event.setText(chatPrefix.append(event.getText()));
    }

    @EventListener
    public void onChatLineAdd(ChatMessageEvent.Visible event)
    {
        chatLineAnims.put(event.getChatLine(), new Animation(300L));
    }

    @EventListener
    public void onMessageIndicator(MessageIndicatorEvent event)
    {
        if (noIndicator.getValue())
        {
            event.cancel();
        }
    }
}

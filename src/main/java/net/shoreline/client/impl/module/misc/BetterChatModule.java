package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.OpenScreenEvent;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.hud.MessageIndicatorEvent;
import net.shoreline.client.impl.event.gui.hud.RenderChatEvent;
import net.shoreline.client.impl.module.client.FontModule;
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
    public static BetterChatModule INSTANCE;

    Config<Boolean> animateConfig = new BooleanConfig.Builder("Animate")
            .setDescription("Animates the chat hud")
            .setDefaultValue(true).build();
    Config<Boolean> timestampConfig = new BooleanConfig.Builder("Timestamp")
            .setDescription("Adds a timestamp to all messages in chat")
            .setDefaultValue(false).build();
    Config<Integer> chatLength = new NumberConfig.Builder<Integer>("MaxLength")
            .setMin(100).setMax(1000).setDefaultValue(500)
            .setDescription("The max number of rows in the chat").build();
    Config<Boolean> noIndicator = new BooleanConfig.Builder("NoIndicator")
            .setDescription("Removes the message indicator")
            .setDefaultValue(false).build();
    Config<Boolean> saveHistory = new BooleanConfig.Builder("SaveHistory")
            .setDescription("Saves chat history when switching between worlds")
            .setDefaultValue(false).build();

    private final Animation chatAnim = new Animation(250L);
    private final ConcurrentMap<ChatHudLine.Visible, Animation> chatLineAnims = new ConcurrentHashMap<>();

    public BetterChatModule()
    {
        super("BetterChat", "Improves in-game chat", GuiCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    @EventListener
    public void onRenderChatText(RenderChatEvent.Text event)
    {
        if (event.getChatLine() == null)
        {
            return;
        }

        if (animateConfig.getValue() && chatLineAnims.containsKey(event.getChatLine()))
        {
            Animation anim = chatLineAnims.get(event.getChatLine());
            anim.setState(true);

            boolean overrideFont = FontModule.INSTANCE.getOverrideChat().getValue();
            double factor = Easing.EXPO_IN_OUT.ease(anim.getFactor());
            int width = overrideFont ? FontManager.FONT.getStringWidth(event.getString()) : mc.textRenderer.getWidth(event.getText());
            int renderX = (int) (event.getX() - (width * (1.0f - factor)));
            int color = ColorUtil.withTransparency(Colors.WHITE, (float) factor);

            event.cancel();
            if (overrideFont)
            {
                FontManager.FONT.drawStringWithShadow(event.getContext().getMatrices(), event.getString(), renderX, event.getY(), color);
            } else
            {
                event.getContext().drawTextWithShadow(mc.textRenderer, event.getText(), renderX, event.getY(), color);
            }
        }
    }

    @EventListener
    public void onRenderChatBackground(RenderChatEvent.Background event)
    {
        if (animateConfig.getValue())
        {
            double factor = Easing.SMOOTH_STEP.ease(chatAnim.getFactor());
            event.cancel();
            event.getContext().fill(event.getX(),
                    event.getY(),
                    event.getWidth(),
                    (int) (event.getY() - (12.0f * factor)),
                    mc.options.getTextBackgroundColor(Integer.MIN_VALUE));
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
            chatPrefix = Text.literal(ClientFormatting.THEME + "<" + time + "> ");
        }

        event.cancel();
        event.setText(chatPrefix.append(event.getText()));
    }

    @EventListener
    public void onChatLineAdd(ChatMessageEvent.Visible event)
    {
        chatLineAnims.put(event.getChatLine(), new Animation(400L));
    }

    @EventListener
    public void onMessageIndicator(MessageIndicatorEvent event)
    {
        if (noIndicator.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onChatOpen(OpenScreenEvent event)
    {
        if (event.getScreen() == null && chatAnim.getState())
        {
            chatAnim.setState(false);
        } else if (event.getScreen() instanceof ChatScreen)
        {
            chatAnim.setState(true);
        }
    }

    public double getChatFactor()
    {
        return isEnabled() && animateConfig.getValue() ? Easing.SMOOTH_STEP.ease(chatAnim.getFactor()) : 1.0f;
    }
}

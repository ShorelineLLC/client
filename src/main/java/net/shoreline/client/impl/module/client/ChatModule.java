package net.shoreline.client.impl.module.client;

import baritone.api.BaritoneAPI;
import net.minecraft.client.gui.screen.ChatScreen;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.gui.chat.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.hud.RenderOverlayEvent;
import net.shoreline.client.impl.event.keyboard.KeyboardInputEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketChatMessage;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @since 1.0
 */
public class ChatModule extends ToggleModule
{
    private static ChatModule INSTANCE;

    private boolean ircChat;
    private boolean notified;
    private final Timer timer = new CacheTimer();

    public Config<Boolean> dmsOnly = register(new BooleanConfig("DMOnly", "Only receive private messages from IRC", false));

    private final Animation ircAnimation = new Animation(false, 200, Easing.LINEAR);

    public ChatModule()
    {
        super("Chat", "Manages the client chat", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static ChatModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null || notified || dmsOnly.getValue())
        {
            return;
        }

        ChatUtil.clientSendMessageRaw("§s[Chat]§7 Press ALT in chat to enter IRC!", 107);
        notified = true;
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        if (dmsOnly.getValue())
        {
            return;
        }

        if (notified)
        {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[Chat]§7 Press ALT in chat to enter IRC!", 107);
        notified = true;
    }

    @EventListener
    public void onKey(KeyboardInputEvent event)
    {
        if (dmsOnly.getValue())
        {
            return;
        }

        if (!timer.passed(250))
        {
            return;
        }
        if (event.getAction() != GLFW.GLFW_REPEAT && (event.getKeycode() == GLFW.GLFW_KEY_LEFT_ALT
                || event.getKeycode() == GLFW.GLFW_KEY_RIGHT_ALT) && mc.currentScreen instanceof ChatScreen)
        {
            ircChat = !ircChat;
            ircAnimation.setState(ircChat);
            timer.reset();
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onChatMessage(ChatMessageEvent.Client event)
    {
        if (dmsOnly.getValue())
        {
            return;
        }

        if (ircChat)
        {
            final String text = event.getMessage().trim();
            if (text.isEmpty() || text.isBlank() || text.startsWith(Managers.COMMAND.getPrefix())
                    || ShorelineMod.isBaritonePresent() && text.startsWith(BaritoneAPI.getSettings().prefix.value) || text.startsWith("/"))
            {
                return;
            }

            event.cancel();
            IRCManager.getInstance().sendPacket(new CPacketChatMessage(text));
            mc.inGameHud.getChatHud().addToMessageHistory(text);
        }
    }

    @EventListener
    public void onRenderOverlay(RenderOverlayEvent.Hotbar event)
    {
        if (dmsOnly.getValue())
        {
            return;
        }

        if (mc.currentScreen instanceof ChatScreen && ircAnimation.getFactor() > 0.01)
        {
            float height = mc.getWindow().getScaledHeight();
            float width = mc.getWindow().getScaledWidth();
            float anim = HUDModule.getInstance().isEnabled() ? HUDModule.getInstance().getChatAnimation() : 1.0f;
            RenderManager.borderedRect(event.getContext().getMatrices(), 2, (int) height,
                    width - 4, -16.0f * anim, ColorsModule.getInstance().getRGB((int) (255.0f * ircAnimation.getFactor())), 1.0f);
        }
    }

    public boolean isDmsOnly()
    {
        return dmsOnly.getValue();
    }
}

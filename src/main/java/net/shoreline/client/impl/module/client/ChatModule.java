package net.shoreline.client.impl.module.client;

import net.minecraft.client.gui.screen.ChatScreen;
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
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @since 1.0
 */
public class ChatModule extends ToggleModule
{
    private boolean ircChat;
    private boolean notified;
    private final Timer timer = new CacheTimer();

    public ChatModule()
    {
        super("Chat", "Manages the client chat", ModuleCategory.CLIENT);
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null || notified)
        {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[Chat]§7 Press ALT to enter IRC chat!", 107);
        notified = true;
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        if (notified)
        {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[Chat]§7 Press ALT to enter IRC chat!", 107);
        notified = true;
    }

    @EventListener
    public void onKey(KeyboardInputEvent event)
    {
        if (!timer.passed(250))
        {
            return;
        }
        if (event.getAction() != GLFW.GLFW_REPEAT && (event.getKeycode() == GLFW.GLFW_KEY_LEFT_ALT
                || event.getKeycode() == GLFW.GLFW_KEY_RIGHT_ALT) && mc.currentScreen instanceof ChatScreen)
        {
            ircChat = !ircChat;
            timer.reset();
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onChatMessage(ChatMessageEvent.Client event)
    {
        if (ircChat)
        {
            final String text = event.getMessage().trim();
            if (text.isEmpty() || text.isBlank() || text.startsWith(Managers.COMMAND.getPrefix()) || text.startsWith("/"))
            {
                return;
            }
            event.cancel();
            IRCManager.getInstance().sendPacket(new CPacketChatMessage(text));
        }
    }

    @EventListener
    public void onRenderOverlay(RenderOverlayEvent.Post event)
    {
        if (mc.currentScreen instanceof ChatScreen && ircChat)
        {
            float height = mc.getWindow().getScaledHeight();
            float width = mc.getWindow().getScaledWidth();
            float anim = HUDModule.getInstance().isEnabled() ? HUDModule.getInstance().getChatAnimation() : 1.0f;
            RenderManager.borderedRect(event.getContext().getMatrices(), 2, (int) (height - 2.0f),
                    width - 4, -12.0f * anim, ColorsModule.getInstance().getRGB(), 1.0f);
        }
    }
}

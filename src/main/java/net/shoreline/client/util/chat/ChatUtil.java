package net.shoreline.client.util.chat;

import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.imixin.IChatHud;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.util.Globals;

/**
 * @author linus
 * @since 1.0
 */
public class ChatUtil implements Globals
{
    //
    public static final String PREFIX = "§s[Shoreline] §r";

    /**
     * Sends a message in the {@link net.minecraft.client.gui.hud.ChatHud}
     * which is not visible to others
     *
     * @param message The message
     */
    public static void clientSendMessage(String message)
    {
        clientSendMessage(message, 0);
    }

    public static void clientSendMessage(String message, int id)
    {
        MessageIndicator messageIndicator = new MessageIndicator(ColorsModule.getInstance().getRGB(), null, Text.empty(), "Shoreline");
        ((IChatHud) mc.inGameHud.getChatHud()).addMessage(Text.of(PREFIX + message), messageIndicator, id);
    }

    /**
     * @param message
     * @param params
     */
    public static void clientSendMessage(String message, Object... params)
    {
        clientSendMessage(String.format(message, params));
    }

    /**
     * @param message
     */
    public static void clientSendMessageRaw(String message)
    {
        clientSendMessageRaw(message, 0);
    }

    public static void clientSendMessageRaw(String message, int id)
    {
        MessageIndicator messageIndicator = new MessageIndicator(ColorsModule.getInstance().getRGB(), null, Text.empty(), "Shoreline");
        ((IChatHud) mc.inGameHud.getChatHud()).addMessage(Text.of(message), messageIndicator, id);
    }

    /**
     * @param message
     * @param params
     */
    public static void clientSendMessageRaw(String message, Object... params)
    {
        clientSendMessageRaw(String.format(message, params));
    }

    /**
     * Sends a message in the {@link net.minecraft.client.network.ClientPlayNetworkHandler}
     * which is visible to others on a server
     *
     * @param message The message
     */
    public static void serverSendMessage(String message)
    {
        if (mc.player != null)
        {
            mc.player.networkHandler.sendChatMessage(message);
        }
    }

    public static void serverSendCommand(String message)
    {
        if (mc.player != null)
        {
            mc.player.networkHandler.sendChatCommand(message);
        }
    }

    /**
     * @param player
     * @param message
     */
    public static void serverSendMessage(PlayerEntity player, String message)
    {
        if (mc.player != null)
        {
            String reply = "/msg " + player.getName().getString() + " ";
            mc.player.networkHandler.sendChatMessage(reply + message);
        }
    }

    /**
     * @param player
     * @param message
     * @param params
     */
    public static void serverSendMessage(PlayerEntity player, String message,
                                         Object... params)
    {
        serverSendMessage(player, String.format(message, params));
    }

    /**
     * @param message
     */
    public static void error(String message)
    {
        clientSendMessage(Formatting.RED + message);
    }

    /**
     * @param message
     * @param params
     */
    public static void error(String message, Object... params)
    {
        clientSendMessage(Formatting.RED + message, params);
    }
}

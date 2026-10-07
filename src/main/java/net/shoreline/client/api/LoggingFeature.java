package net.shoreline.client.api;

import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.imixin.IChatHud;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.ClientFormatting;

public class LoggingFeature extends ListeningFeature
{
    protected static final String RAW_PREFIX = "[Shoreline]";
    protected static final String PREFIX = ClientFormatting.THEME + RAW_PREFIX + Formatting.RESET + " ";
    protected static final String ERROR_PREFIX = "[\u274C] ";
    protected static final String SUCCESS_PREFIX = "[\u2713] ";

    public LoggingFeature(String name)
    {
        super(name);
    }

    public LoggingFeature(String name, String[] nameAliases)
    {
        super(name, nameAliases);
    }

    protected void sendClientMessageWithOptionalDeletion(String message, int id)
    {
        sendChatMessageWithOptionalDeletion(PREFIX + message, ThemeModule.INSTANCE.getPrimaryColor().getRGB(), id);
    }

    protected void sendClientChatMessage(String message)
    {
        sendChatMessage(PREFIX + message, ThemeModule.INSTANCE.getPrimaryColor().getRGB());
    }

    protected void sendClientTextMessage(Text text)
    {
        sendChatText(Text.literal(PREFIX).append(text), ThemeModule.INSTANCE.getPrimaryColor().getRGB());
    }

    protected void sendChatMessage(String message)
    {
        sendChatMessage(message, ThemeModule.INSTANCE.getPrimaryColor().getRGB());
    }

    protected void sendChatText(Text text)
    {
        sendChatText(text, ThemeModule.INSTANCE.getPrimaryColor().getRGB());
    }

    protected void sendSuccessChatMessage(String message)
    {
        sendChatMessage(Formatting.GREEN + SUCCESS_PREFIX + message, Colors.GREEN);
    }

    protected void sendErrorChatMessage(String message)
    {
        sendChatMessage(Formatting.RED + ERROR_PREFIX + message, Colors.RED);
    }

    protected void sendChatMessage(String message, int color)
    {
        sendChatText(Text.of(message), color);
    }

    protected void sendChatText(Text text, int color)
    {
        runOnThread(() -> mc.inGameHud.getChatHud().addMessage(text, null,
                new MessageIndicator(color, null, Text.empty(), "CLIENT")));
    }

    protected void sendChatMessageWithOptionalDeletion(String message, int color, int id)
    {
        runOnThread(() -> ((IChatHud) mc.inGameHud.getChatHud()).addMessage(Text.of(message),
                new MessageIndicator(color, null, Text.empty(), "CLIENT"), id));
    }
}

package net.shoreline.client.api;

import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.imixin.IChatHud;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.ClientFormatting;

public class LoggingFeature extends GenericFeature
{
    protected static final String RAW_PREFIX = "[Shoreline]";
    protected static final String PREFIX = ClientFormatting.THEME + RAW_PREFIX + " ";
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
        sendChatMessageWithOptionalDeletion(PREFIX + Formatting.RESET + message, ThemeModule.INSTANCE.getPrimaryColor().getRGB(), id);
    }

    protected void sendClientChatMessage(String message)
    {
        sendChatMessage(PREFIX + Formatting.RESET + message, ThemeModule.INSTANCE.getPrimaryColor().getRGB());
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
        mc.inGameHud.getChatHud().addMessage(Text.of(message), null,
                new MessageIndicator(color, null, Text.empty(), "CLIENT"));
    }

    protected void sendChatMessageWithOptionalDeletion(String message, int color, int id)
    {
        ((IChatHud) mc.inGameHud.getChatHud()).addMessage(Text.of(message),
                new MessageIndicator(color, null, Text.empty(), "CLIENT"), id);
    }
}

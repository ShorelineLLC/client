package net.shoreline.client.api;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.render.ClientFormatting;

public class LoggingFeature extends GenericFeature
{
    private static final String PREFIX = ClientFormatting.CLIENT + "[Shoreline] ";
    private static final String ERROR_PREFIX = "[\u274C] ";
    private static final String SUCCESS_PREFIX = "[\u2713] ";

    public LoggingFeature(String name)
    {
        super(name);
    }

    public LoggingFeature(String name, String[] nameAliases)
    {
        super(name, nameAliases);
    }

    protected void sendClientChatMessage(String message)
    {
        sendChatMessage(PREFIX + Formatting.RESET + message);
    }

    protected void sendSuccessChatMessage(String message)
    {
        sendChatMessage(Formatting.GREEN + SUCCESS_PREFIX + message);
    }

    protected void sendErrorChatMessage(String message)
    {
        sendChatMessage(Formatting.RED + ERROR_PREFIX + message);
    }

    protected void sendChatMessage(String message)
    {
        mc.inGameHud.getChatHud().addMessage(Text.of(message));
    }
}

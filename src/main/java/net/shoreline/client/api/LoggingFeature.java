package net.shoreline.client.api;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LoggingFeature extends GenericFeature
{
    private static final String PREFIX = "[Shoreline] ";
    private static final String ERROR_PREFIX = "[\u274C] ";

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

    protected void sendErrorChatMessage(String message)
    {
        sendChatMessage(Formatting.RED + ERROR_PREFIX + message);
    }

    protected void sendChatMessage(String message)
    {
        mc.inGameHud.getChatHud().addMessage(Text.of(message));
    }
}

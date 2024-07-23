package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author linus
 * @since 1.0
 */
public class AntiSpamModule extends ToggleModule
{

    // TODO: ADD MORE SPAM CHECKS
    Config<Boolean> unicodeConfig = register(new BooleanConfig("Unicode", "Prevents unicode characters from being rendered in chat", false));
    //
    private final Map<UUID, String> messages = new HashMap<>();

    public AntiSpamModule()
    {
        super("AntiSpam", "Prevents players from spamming the game chat",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (event.getPacket() instanceof ChatMessageS2CPacket packet)
        {
            if (unicodeConfig.getValue())
            {
                String msg = packet.body().content();
                Pattern pattern = Pattern.compile("[^\\x00-\\x7F\\u0400-\\u04FF]+", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find())
                {
                    event.cancel();
                    return;
                }
            }
            // prevents same message spam
            final UUID sender = packet.sender();
            final String chatMessage = packet.body().content();
            String lastMessage = messages.get(sender);
            if (chatMessage.equalsIgnoreCase(lastMessage))
            {
                event.cancel();
            }
            else if (lastMessage != null)
            {
                messages.replace(sender, chatMessage);
            }
            else
            {
                messages.put(sender, chatMessage);
            }
        }
    }
}

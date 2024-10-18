package net.shoreline.client.impl.module.misc;

import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.math.HexRandom;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class SpammerModule extends ToggleModule
{
    Config<Float> delayConfig = register(new NumberConfig<>("Delay", "The chat message delay", 0.0f, 1.5f, 10.0f));
    Config<Boolean> randomConfig = register(new BooleanConfig("Random", "Randomizes the spammed messages", false));
    Config<Boolean> antiKickConfig = register(new BooleanConfig("AntiKick", "Adds a random suffix to end of messages to prevent kicks", false));
    private final List<String> messages = new ArrayList<>();
    private int messageIndex;
    private final Timer spamTimer = new CacheTimer();

    public SpammerModule()
    {
        super("Spammer", "Spams messages in the chat", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        loadFile();
        messageIndex = 0;
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        onEnable();
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        if (spamTimer.passed(delayConfig.getValue() * 1000.0f))
        {
            sendSpamMessage(getSpammerMessage());
            spamTimer.reset();
        }
    }

    private void loadFile()
    {
        File spammerDir = Shoreline.CONFIG.getClientDirectory().resolve("spammer.txt").toFile();
        if (!spammerDir.exists())
        {
            sendModuleError("The spammer.txt file does not exist! Please create one to enable this module");
            disable();
            return;
        }
        messages.clear();
        try
        {
            for (String line : Files.readAllLines(Path.of(spammerDir.getAbsolutePath()), StandardCharsets.UTF_8))
            {
                String[] messages1 = line.split(",");
                for (String message : messages1)
                {
                    messages.add(message.trim());
                }
            }
        }
        catch (IOException e)
        {

        }
    }

    private String getSpammerMessage()
    {
        String defaultMessage = "Shoreline victory!";
        if (messages.isEmpty())
        {
            return defaultMessage;
        }
        if (randomConfig.getValue())
        {
            String message = messages.get(RANDOM.nextInt(messages.size()));
            if (message != null)
            {
                if (antiKickConfig.getValue())
                {
                    message += " " + HexRandom.generateRandomHex(2);
                }
                return message;
            }
        }
        else
        {
            String message = messages.get(messageIndex);
            messageIndex++;
            if (messageIndex >= messages.size())
            {
                messageIndex = 0;
            }

            if (message != null)
            {
                if (antiKickConfig.getValue())
                {
                    message += " " + HexRandom.generateRandomHex(2);
                }
                return message;
            }
        }
        return defaultMessage;
    }

    private void sendSpamMessage(String message)
    {
        if (message.charAt(0) == '/')
        {
            message = message.substring(1);
            mc.player.networkHandler.sendCommand(message);
            return;
        }
        if (message.startsWith(Managers.COMMAND.getPrefix()))
        {
            String literal = message.substring(1);
            mc.inGameHud.getChatHud().addToMessageHistory(message);
            try
            {
                Managers.COMMAND.getDispatcher().execute(Managers.COMMAND.getDispatcher().parse(literal, Managers.COMMAND.getSource()));
            }
            catch (Exception exception)
            {
                // exception.printStackTrace();
            }
            return;
        }
        if (mc.isInSingleplayer())
        {
            ChatUtil.clientSendMessageRaw(message);
        }
        else
        {
            ChatUtil.serverSendMessage(message);
        }
    }
}

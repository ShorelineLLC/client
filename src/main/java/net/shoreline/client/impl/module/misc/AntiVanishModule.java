package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.util.*;
import java.util.stream.Collectors;

public class AntiVanishModule extends ToggleModule
{
    //
    private final Timer vanishTimer = new CacheTimer();
    private final Set<String> messageCache = new HashSet<>();
    //
    private Map<UUID, String> playerCache = new HashMap<>();

    public AntiVanishModule()
    {
        super("AntiVanish", "Notifies user when a player uses /vanish", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        messageCache.clear();
    }

    // TODO: Find a better way to do this
    @EventListener
    public void onChatMessage(ChatMessageEvent event)
    {
        // This only works if the server doesnt have a custom join/leave
        // message plugin
        String message = event.getText().getString();
        if (message.contains("left"))
        {
            messageCache.add(message);
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        if (!vanishTimer.passed(1000))
        {
            return;
        }
        final Map<UUID, String> players = playerCache;
        playerCache = mc.getNetworkHandler().getPlayerList().stream()
                .collect(Collectors.toMap(e -> e.getProfile().getId(), e -> e.getProfile().getName()));
        for (UUID uuid : players.keySet())
        {
            if (playerCache.containsKey(uuid))
            {
                continue;
            }
            String name = players.get(uuid);
            if (messageCache.stream().noneMatch(s -> s.contains(name)))
            {
                sendModuleMessage("%s used /vanish!", name);
            }
        }
        messageCache.clear();
        vanishTimer.reset();
    }
}

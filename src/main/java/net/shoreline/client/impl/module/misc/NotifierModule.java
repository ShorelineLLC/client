package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ModuleToggleEvent;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.combat.TotemPopEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.render.ClientFormatting;
import net.shoreline.eventbus.annotation.EventListener;

public class NotifierModule extends Toggleable
{
    Config<Boolean> totemPops = new BooleanConfig.Builder("TotemPop")
            .setDescription("Notifies when a nearby player pops a totem")
            .setDefaultValue(false).build();

    public NotifierModule()
    {
        super("Notifier", new String[] {"ChatNotifier"}, "Notifies in chat", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTotemPop(TotemPopEvent event)
    {
        if (!totemPops.getValue() || event.getEntity() == mc.player
                || !(event.getEntity() instanceof LivingEntity e))
        {
            return;
        }

        String playerName = formatPlayerName(e.getName().getString());
        String popNotification = String.format("%s "
                        + Formatting.WHITE + "popped "
                        + ClientFormatting.CLIENT + "%d "
                        + Formatting.WHITE + "totem%s",
                playerName, event.getPops(), event.getPops() > 1 ? "s" : "");

        sendClientChatMessage(popNotification);
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (!totemPops.getValue() || event.getEntity() == mc.player
                || !(event.getEntity() instanceof LivingEntity e))
        {
            return;
        }

        String playerName = formatPlayerName(e.getName().getString());
        String deathNotification = String.format("%s "
                        + Formatting.WHITE + "died after popping "
                        + ClientFormatting.CLIENT + "%d "
                        + Formatting.WHITE + "totem%s",
                playerName, event.getPops(), event.getPops() > 1 ? "s" : "");

        sendClientChatMessage(deathNotification);
    }

    @EventListener
    public void onModuleToggle(ModuleToggleEvent event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.isEnabled())
        {
            sendClientChatMessage(Formatting.GRAY + event.getModule().getName() + Formatting.GREEN + " enabled");
        } else
        {
            sendClientChatMessage(Formatting.GRAY + event.getModule().getName() + Formatting.RED + " disabled");
        }
    }

    private String formatPlayerName(String name)
    {
        return Formatting.GRAY + name;
    }
}

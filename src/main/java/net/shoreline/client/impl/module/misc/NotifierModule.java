package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ModuleToggleEvent;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
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
        if (!totemPops.getValue() || event.getEntity() == mc.player || event.getPops() <= 0
                || !(event.getEntity() instanceof LivingEntity e))
        {
            return;
        }

        String playerName = formatPlayerName(e.getName().getString());
        String popNotification = String.format("%s "
                        + Formatting.WHITE + "popped "
                        + ClientFormatting.THEME + "%d "
                        + Formatting.WHITE + "totem%s",
                playerName, event.getPops(), event.getPops() > 1 ? "s" : "");

        sendClientMessageWithOptionalDeletion(popNotification, e.hashCode());
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (!totemPops.getValue() || event.getEntity() == mc.player || event.getPops() <= 0
                || !(event.getEntity() instanceof LivingEntity e))
        {
            return;
        }

        String playerName = formatPlayerName(e.getName().getString());
        String deathNotification = String.format("%s "
                        + Formatting.WHITE + "died after popping "
                        + ClientFormatting.THEME + "%d "
                        + Formatting.WHITE + "totem%s",
                playerName, event.getPops(), event.getPops() > 1 ? "s" : "");

        sendClientMessageWithOptionalDeletion(deathNotification, e.hashCode());
    }

    @EventListener
    public void onModuleToggle(ModuleToggleEvent event)
    {
        if (checkNull() || !event.getModule().shouldNotify())
        {
            return;
        }

        if (event.isEnabled())
        {
            sendClientMessageWithOptionalDeletion(Formatting.GRAY + event.getModule().getName() + Formatting.GREEN + " enabled", event.getModule().hashCode());
        } else
        {
            sendClientMessageWithOptionalDeletion(Formatting.GRAY + event.getModule().getName() + Formatting.RED + " disabled", event.getModule().hashCode());
        }
    }

    private String formatPlayerName(String name)
    {
        return (Managers.SOCIAL.isFriend(name) ? ClientFormatting.FRIEND : Formatting.GRAY) + name;
    }
}

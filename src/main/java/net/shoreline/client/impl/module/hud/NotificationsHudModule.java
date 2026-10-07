package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.Colors;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.impl.combat.TotemPopEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

// these are so ugly
public class NotificationsHudModule extends DynamicHudModule
{
    Config<Integer> maxNotifications = new NumberConfig.Builder<Integer>("Max")
            .setMin(1).setDefaultValue(5).setMax(50)
            .setDescription("The max amount of notifications that can be visible at once")
            .build();

    private final Map<NotificationsEntry, Long> notificationsMap = new HashMap<>();

    public NotificationsHudModule()
    {
        super("Notifications", "Displays notifications on the HUD", 200, 200);
    }

    @Override
    public void loadEntries() {}

    @Override
    public void cacheWidth()
    {
        int result = 0;
        for (DynamicEntry entry : getHudEntries())
        {
            if (entry.isDrawing() || !entry.isDone())
            {
                result = Math.max(result, getTextWidth(entry.getText().get()));
            }
        }

        setWidth(result + 10);
    }

    @Override
    public void drawEntries(DrawContext context, float tickDelta)
    {
        setOffset(0);
        sortEntries();
        for (DynamicEntry entry : getHudEntries())
        {
            if (entry.isDrawing() || !entry.isDone())
            {
                entry.draw(context, getX() + (isLeft() ? 0 : getWidth()), getY(), offset, tickDelta);
            }
            else
            {
                getHudEntries().remove(entry);
            }
        }
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        Iterator<Map.Entry<NotificationsEntry, Long>> iterator = notificationsMap.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<NotificationsEntry, Long> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getValue() > 5000)
            {
                NotificationsEntry notificationsEntry = entry.getKey();
                notificationsEntry.setDrawing(() -> false);
                iterator.remove();
            }
        }
    }

    @EventListener
    public void onPop(TotemPopEvent event)
    {
        Entity entity = event.getEntity();
        if (entity != null)
        {
            String name = entity.getName().getString();
            int pops = event.getPops();
            addNotification(name + " popped " + pops + " totem" + (pops > 1 ? "s" : ""));
        }
    }

    public void addNotification(String notification)
    {
        if (notificationsMap.size() > maxNotifications.getValue())
        {
            NotificationsEntry oldestEntry = null;
            long oldestTime = Long.MAX_VALUE;

            for (Map.Entry<NotificationsEntry, Long> entry : notificationsMap.entrySet())
            {
                if (entry.getValue() < oldestTime)
                {
                    oldestTime = entry.getValue();
                    oldestEntry = entry.getKey();
                }
            }

            if (oldestEntry != null)
            {
                notificationsMap.remove(oldestEntry);
                oldestEntry.setDrawing(() -> false);
            }
        }

        NotificationsEntry entry = new NotificationsEntry(this, () -> notification);
        getHudEntries().add(entry);
        notificationsMap.put(entry, System.currentTimeMillis());
    }

    private class NotificationsEntry extends DynamicEntry
    {
        public NotificationsEntry(DynamicHudModule mod, Supplier<String> text)
        {
            super(mod, text, () -> true);
            this.setHeight(20);
        }

        @Override
        public void drawText(DrawContext context, String string, float x, float y)
        {
            boolean left = getModule().isLeft();
            float width = getModule().getTextWidth(string) + 10;
            float padding = left ? 5 : -5;
            context.fill((int) (x + padding), (int) y + 2, (int) (x + padding + 2), (int) (y + getHeight() - 2), ColorUtil.withTransparency(ThemeModule.INSTANCE.getPrimaryColor(), (float) getYAnimation().getFactor()));
            context.fill((int) (x + padding + 2), (int) y + 2, (int) (x + padding + width), (int) (y + getHeight() - 2), ColorUtil.withTransparency(0xAA000000, (float) getYAnimation().getFactor()));
            getModule().drawTextTransparency(context.getMatrices(), string, x + (left ? padding * 2 : 0), y + (getHeight() / 2f) - 2, (float) getYAnimation().getFactor());
        }
    }
}

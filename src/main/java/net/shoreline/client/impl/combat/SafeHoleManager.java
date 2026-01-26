package net.shoreline.client.impl.combat;

import net.shoreline.client.Shoreline;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.combat.hole.HoleData;
import net.shoreline.client.impl.combat.hole.HoleScanner;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.FillerModule;
import net.shoreline.client.impl.module.render.HoleESPModule;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.Collection;

public class SafeHoleManager extends AsyncFeature<Collection<HoleData>>
{
    private final HoleScanner scanner = new HoleScanner();

    public SafeHoleManager()
    {
        super("Holes", new ArrayList<>());
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (!HoleESPModule.INSTANCE.isEnabled() && !FillerModule.INSTANCE.isEnabled())
        {
            currentResult = null;
            return;
        }

        if (currentResult == null || currentResult.isDone())
        {
            scanner.createCube(mc.world, EntityUtil.getRoundedBlockPos(mc.player));
            runAsync(scanner::scanHoles);
        }
    }
}

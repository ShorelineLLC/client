package net.shoreline.client.impl.combat;

import net.shoreline.client.impl.async.AsyncFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.render.HoleESPModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class SafeHoleManager extends AsyncFeature<HoleData>
{
    private final HoleScanner scanner = new HoleScanner();

    public SafeHoleManager()
    {
        super("Holes");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        if (!HoleESPModule.INSTANCE.isEnabled())
        {
            return;
        }

        if (currentResult == null || currentResult.isDone())
        {
            scanner.createSphere(mc.world, mc.player.getBlockPos());

            runAsync(scanner::scanHoles);
        }
    }
}

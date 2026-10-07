package net.shoreline.client.impl.module.combat.anchor;

import lombok.Getter;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.AnchorAuraModule;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

@Getter
public class AnchorManager extends AsyncFeature<Collection<AnchorData>>
{
    private final AnchorAuraModule module;
    private final AnchorScanner scanner;

    public AnchorManager(AnchorAuraModule module)
    {
        super("Anchors", new ArrayList<>());
        this.module = module;
        this.scanner = new AnchorScanner(module);
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTick_Pre(TickEvent.Pre event)
    {
        if (check())
        {
            currentResult = null;
            return;
        }

        if (currentResult == null || currentResult.isDone())
        {
            scanner.createSphere(mc.world, EntityUtil.getRoundedBlockPos(mc.player));
            scanner.createEntityLookup(mc.world, mc.player.getEyePos());
            runAsync(() -> new TreeSet<>(scanner.getData()));
        }
    }

    public boolean check()
    {
        if (checkNull())
        {
            return true;
        }

        return !module.isEnabled();
    }
}

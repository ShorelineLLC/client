package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.shoreline.client.api.LoggingFeature;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class CrystalOptimizer extends LoggingFeature
{
    private final Set<Integer> deadCrystals = new ConcurrentSkipListSet<>();

    public CrystalOptimizer()
    {
        super("Crystal Optimizer");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onRenderEntity(RenderEntityWorldEvent event)
    {
        if (event.getEntity() instanceof EndCrystalEntity && isDead(event.getEntity()))
        {
            event.cancel();
        }
    }

    public boolean isDead(Entity entity)
    {
        return deadCrystals.contains(entity.getId());
    }

    public void setDead(int id)
    {
        deadCrystals.add(id);
    }
}

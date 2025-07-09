package net.shoreline.client.impl.manager;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class CombatTargeter extends GenericFeature
{
    private final float targetRange;

    /** All combat modules share the same target **/
    @Getter
    private Entity target;

    public CombatTargeter(float targetRange)
    {
        super("Combat Target");
        this.targetRange = targetRange;
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTickPre(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        double damage = 0.0;
        for (Entity entity : mc.world.getEntities())
        {
            if (!entity.isAlive() || entity.equals(mc.player))
            {
                continue;
            }

            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > targetRange)
            {
                continue;
            }

            double potentialDmg = potentialDamageToTarget(entity);
            if (potentialDmg > damage)
            {
                target = entity;
                damage = potentialDmg;
            }
        }
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        target = null;
    }

    private double potentialDamageToTarget(Entity entity)
    {
        if (AutoMineModule.INSTANCE.isEnabled())
        {

        }

        return 0.0;
    }
}

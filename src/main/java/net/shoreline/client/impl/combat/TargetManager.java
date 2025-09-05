package net.shoreline.client.impl.combat;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.eventbus.EventBus;

public class TargetManager extends GenericFeature
{
    /** All combat modules share the same target **/
    @Getter
    private PlayerEntity target;

    public TargetManager()
    {
        super("Combat Target");
        EventBus.INSTANCE.subscribe(this);
    }

    public void clearTarget()
    {
        target = null;
    }

    public PlayerEntity setClosestTarget(float targetRange)
    {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlayerEntity entity : mc.world.getPlayers())
        {
            if (entity == mc.player || !entity.isAlive() || entity.isRemoved() || Managers.SOCIAL.isFriend(entity))
            {
                continue;
            }

            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > targetRange * targetRange)
            {
                continue;
            }

            if (dist < bestDist)
            {
                best = entity;
                bestDist = dist;
            }
        }

        return target = best;
    }

    public boolean hasTarget()
    {
        return target != null;
    }
}

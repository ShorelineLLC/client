package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.EntitySpawnEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.module.combat.util.PearlExtrapolation;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.util.math.Extrapolation;
import net.shoreline.client.util.math.TrajectoryUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.*;

public class PearlBlockerModule extends ObsidianPlacerModule
{
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Float> placeDistance = new NumberConfig.Builder<Float>("Distance")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(1.5f).setFormat("m")
            .setDescription("Range from the pearl to place the blocking position").build();
    Config<Integer> extrapolateTicks = new NumberConfig.Builder<Integer>("Extrapolate")
            .setMin(0).setDefaultValue(20).setMax(100).setFormat(" ticks")
            .setDescription("The number of ticks ahead to predict pearl velocity").build();

    private final List<Integer> thrownPearls = new ArrayList<>();

    public PearlBlockerModule()
    {
        super("PearlBlocker", "Blocks thrown ender pearls", GuiCategory.COMBAT);
    }

    @EventListener
    public void onUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull() || thrownPearls.isEmpty())
        {
            return;
        }

        int obbySlot = findBestObbySlot();
        if (obbySlot == -1)
        {
            return;
        }

        placements.clear();
        Iterator<Integer> iterator = thrownPearls.iterator();
        while (iterator.hasNext())
        {
            Integer id = iterator.next();
            Entity entity = mc.world.getEntityById(id);
            if (entity == null || entity.horizontalCollision)
            {
                iterator.remove();
                continue;
            }

            Vec3d position = entity.getPos();
            Vec3d extrapolation = MovementExtrapolation.extrapolatePosition(mc.world,
                    box -> mc.world.getBlockCollisions(entity, box),
                    entity.getVelocity(),
                    entity.getBoundingBox(),
                    extrapolateTicks.getValue(),
                    true);

            Vec3d motionDir = extrapolation.subtract(position).normalize();
            Vec3d targetPos = position.add(motionDir.multiply(placeDistance.getValue()));
            placements.add(BlockPos.ofFloored(targetPos));
            iterator.remove();
        }

        if (placements.isEmpty() || !Managers.INTERACT.startPlacement(obbySlot))
        {
            return;
        }

        for (BlockPos placement : placements)
        {
            placeObby(placement);
        }

        Managers.INTERACT.endPlacement();
    }

    @EventListener
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getType() == EntityType.ENDER_PEARL)
        {
            thrownPearls.add(event.getEntityId());
        }
    }
}
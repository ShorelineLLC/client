package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.EntitySpawnEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
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

            List<Vec3d> trajectory = TrajectoryUtil.getPearlTrajectory(entity, extrapolateTicks.getValue() * 2).reversed();
            for (Vec3d vec : trajectory)
            {
                if (mc.player.squaredDistanceTo(vec) > MathHelper.square(placeRange.getValue()))
                {
                    continue;
                }

                BlockPos pos = BlockPos.ofFloored(vec);
                Direction dir = InteractDirection.getInteractDirection(pos);
                if (dir == null)
                {
                    for (Direction direction : Direction.values())
                    {
                        BlockPos offset = pos.offset(direction);
                        Direction helpingDir = InteractDirection.getInteractDirection(offset);
                        if (helpingDir != null)
                        {
                            placements.add(offset);
                            break;
                        }
                    }
                }

                placements.add(pos);
                break;
            }
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
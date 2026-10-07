package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.module.combat.trap.TrapLayer;
import net.shoreline.client.impl.module.combat.trap.TrapModule;
import net.shoreline.client.impl.module.combat.trap.TrapSpec;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.EnumSet;
import java.util.Map;

public class AutoTrapModule extends TrapModule
{
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();

    Config<Float> targetRange = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(15.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to target entities").build();
    Config<Integer> extrapolateTicks = new NumberConfig.Builder<Integer>("Extrapolate")
            .setMin(0).setDefaultValue(0).setMax(10).setFormat(" ticks")
            .setDescription("The number of ticks ahead to predict movement").build();
    Config<Boolean> feetConfig = new BooleanConfig.Builder("Feet")
            .setDescription("Traps player feet")
            .setDefaultValue(true).build();
    Config<Boolean> bodyConfig = new BooleanConfig.Builder("Body")
            .setDescription("Traps player body")
            .setDefaultValue(true).build();
    Config<Boolean> headConfig = new BooleanConfig.Builder("Head")
            .setDescription("Traps player head")
            .setDefaultValue(false).build();
    Config<Boolean> crawlConfig = new BooleanConfig.Builder("Crawl")
            .setDescription("Attempts to prevent target from standing up")
            .setDefaultValue(false).build();
    Config<Boolean> supportConfig = new BooleanConfig.Builder("Support")
            .setDescription("Supporting blocks for autotrap")
            .setDefaultValue(true).build();
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetRange, extrapolateTicks, feetConfig, bodyConfig, headConfig, crawlConfig).build();

    Config<Boolean> instantReplace = new BooleanConfig.Builder("InstantReplace")
            .setDescription("Replaces instantly after mined")
            .setDefaultValue(false).build();
    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Disables when player y-level changes")
            .setDefaultValue(false).build();

    private PlayerEntity trapTarget;

    public AutoTrapModule()
    {
        super("AutoTrap", "Traps enemies with obsidian", GuiCategory.COMBAT);
    }

    @Override
    public String getModuleData()
    {
        return trapTarget != null ? String.valueOf(fadeOutAnimations.size()) : super.getModuleData();
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        int obbySlot = findBestObbySlot();
        if (obbySlot == -1)
        {
            return;
        }

        if (Managers.TARGETING.hasTarget())
        {
            trapTarget = Managers.TARGETING.getTarget();
        } else
        {
            trapTarget = Managers.TARGETING.getClosestTarget(targetRange.getValue());
        }

        if (trapTarget == null)
        {
            return;
        }

        Vec3d targetPos = extrapolateTicks.getValue() <= 0 ? trapTarget.getPos() :
                MovementExtrapolation.extrapolatePosition(mc.world,
                        box -> mc.world.getBlockCollisions(trapTarget, box),
                        trapTarget.getVelocity(),
                        trapTarget.getBoundingBox(),
                        extrapolateTicks.getValue(),
                        false);

        final Box playerBox = trapTarget.getBoundingBox(EntityPose.STANDING).offset(targetPos);
        Box boundingBox = playerBox.withMinY(Math.round(playerBox.minY)).shrink(0.01, 0.1, 0.01);

        TrapSpec trapSpec = TrapSpec.builder().layers(getLayers()).build();
        trapPos.calcTrap(boundingBox, trapSpec);

        placements.clear();
        for (Map.Entry<BlockPos, TrapLayer> entry : trapPos.getTrapPositionLayers().entrySet())
        {
            BlockPos blockPos = entry.getKey();
            double dist = mc.player.squaredDistanceTo(blockPos.toCenterPos());
            if (dist > placeRange.getValue() * placeRange.getValue())
            {
                continue;
            }

            if (!mc.world.getBlockState(blockPos).isReplaceable())
            {
                if (!instantReplace.getValue() || Managers.MINING.getMiningProgress(blockPos) <= 0.5f)
                {
                    continue;
                }
            }

            if (!Managers.INTERACT.canPlaceBlock(blockPos, getCurrentObbyBlock()))
            {
                continue;
            }

            placements.add(blockPos);
        }

        if (placements.isEmpty() || !Managers.INTERACT.startPlacement(obbySlot))
        {
            return;
        }

        for (BlockPos placement : placements)
        {
            Direction direction = InteractDirection.getInteractDirection(placement);
            if (direction == null && supportConfig.getValue())
            {
                for (Direction dir : Direction.values())
                {
                    BlockPos offset = placement.offset(dir);
                    if (placeBlock(offset, Blocks.OBSIDIAN))
                    {
                        break;
                    }
                }
            }

            placeObby(placement);
        }

        Managers.INTERACT.endPlacement();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && packet.getState().isAir() && instantReplace.getValue())
        {
            BlockPos blockPos = packet.getPos();
            if (trapPos.getTrapPositions().contains(blockPos))
            {
                runSingleObbyPlacement(blockPos);
            }
        }
    }

    @Override
    public EnumSet<TrapLayer> getLayers()
    {
        EnumSet<TrapLayer> layers = EnumSet.noneOf(TrapLayer.class);
        if (feetConfig.getValue())
        {
            layers.add(TrapLayer.FEET);
        } if (bodyConfig.getValue())
        {
            layers.add(TrapLayer.BODY);
        } if (headConfig.getValue())
        {
            layers.add(TrapLayer.CEILING);
        } if (crawlConfig.getValue())
        {
            layers.add(TrapLayer.BODY_INTERSECT);
            layers.add(TrapLayer.FLOOR);
        }

        return layers;
    }
}

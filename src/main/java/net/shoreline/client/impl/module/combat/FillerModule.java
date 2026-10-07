package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.hole.HoleData;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Arrays;
import java.util.Collection;

public class FillerModule extends ObsidianPlacerModule
{
    public static FillerModule INSTANCE;

    Config<FillMode> fillMode = new EnumConfig.Builder<FillMode>("Mode")
            .setValues(FillMode.values())
            .setDescription("The area to fill")
            .setDefaultValue(FillMode.HOLES).build();
    Config<Float> targetRange = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(15.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to target entities").build();
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Integer> extrapolateTicks = new NumberConfig.Builder<Integer>("Extrapolate")
            .setMin(0).setDefaultValue(0).setMax(10).setFormat(" ticks")
            .setDescription("The number of ticks ahead to predict movement").build();
    Config<Float> smartRange = new NumberConfig.Builder<Float>("SmartRange")
            .setMin(0.0f).setMax(6.0f).setDefaultValue(2.0f).setFormat("m")
            .setDescription("If hole is within this distance of a hole we will fill that hole")
            .build();
    Config<Float> safetyRange = new NumberConfig.Builder<Float>("SafetyRange")
            .setMin(0.0f).setMax(4.0f).setDefaultValue(2.0f).setFormat("m")
            .setDescription("If you are within this distance of the hole, it will not be filled")
            .build();
    Config<Boolean> smartFill = new ToggleableConfigGroup.Builder("Smart")
            .addAll(smartRange, safetyRange)
            .setDefaultValue(true)
            .setDescription("Only fills holes that are close to players")
            .setVisible(() -> fillMode.getValue() == FillMode.HOLES)
            .build();
    Config<Boolean> doublesConfig = new BooleanConfig.Builder("Doubles")
            .setDescription("Fills in double holes")
            .setVisible(() -> fillMode.getValue() == FillMode.HOLES)
            .setDefaultValue(false).build();
    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Disables after filling")
            .setDefaultValue(false).build();

    public FillerModule()
    {
        super("Filler", new String[] {"HoleFill"}, "Fills in blocks around you", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(PlayerUpdateEvent.Pre event)
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

        PlayerEntity target;
        if (Managers.TARGETING.hasTarget())
        {
            target = Managers.TARGETING.getTarget();
        }
        else
        {
            target = Managers.TARGETING.getClosestTarget(targetRange.getValue());
        }

        if (target == null)
        {
            return;
        }

        Vec3d targetPos = extrapolateTicks.getValue() <= 0 ? target.getPos() :
            MovementExtrapolation.extrapolatePosition(mc.world,
                box -> mc.world.getBlockCollisions(target, box),
                target.getVelocity(),
                target.getBoundingBox(),
                extrapolateTicks.getValue(),
                false);

        placements.clear();
        if (fillMode.getValue() == FillMode.HOLES)
        {
            Collection<HoleData> latestHoleData = getFilteredHoles(targetPos);
            for (HoleData data : latestHoleData)
            {
                placements.addAll(Arrays.asList(data.getHolePos()));
            }
        }
        else
        {
            BlockPos playerPos = BlockPos.ofFloored(targetPos.getX(), targetPos.getY() + 0.08, targetPos.getZ()).down();
            for (Direction direction : Direction.Type.HORIZONTAL)
            {
                BlockPos pos = playerPos.offset(direction);
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) > MathHelper.square(placeRange.getValue()))
                {
                    continue;
                }

                placements.add(pos);
            }
        }

        for (BlockPos placement : placements)
        {
            runSingleBlockPlacement(placement, Blocks.OBSIDIAN, obbySlot);
        }

        if (autoDisable.getValue())
        {
            disable();
        }
    }

    public Collection<HoleData> getFilteredHoles(Vec3d targetPos)
    {
        Collection<HoleData> latestHoleData = Managers.HOLE.getResults();
        latestHoleData.removeIf(hole ->
                hole.checkRange(mc.player.getPos(), placeRange.getValue())
                        || (hole.getHolePos().length > 1 && !doublesConfig.getValue()));

        if (smartFill.getValue())
        {
            latestHoleData.removeIf(hole ->
            {
                if (hole.checkRange(targetPos, smartRange.getValue()))
                {
                    return true;
                }

                return !hole.checkRange(mc.player.getPos(), safetyRange.getValue());
            });

            return latestHoleData;
        }

        return latestHoleData;
    }

    public boolean shouldGetDoubles()
    {
        return isEnabled() && doublesConfig.getValue();
    }

    public enum FillMode
    {
        HOLES,
        FLATTEN
    }
}

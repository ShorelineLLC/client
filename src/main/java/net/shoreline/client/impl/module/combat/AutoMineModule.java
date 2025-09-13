package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.TickPriorities;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.mining.MiningUtil;
import net.shoreline.client.impl.module.combat.trap.TrapLayer;
import net.shoreline.client.impl.module.combat.trap.TrapModule;
import net.shoreline.client.impl.module.combat.trap.TrapSpec;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.entity.PlayerUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.EnumSet;
import java.util.Map;

public class AutoMineModule extends TrapModule
{
    public static AutoMineModule INSTANCE;
    private static SpeedMineModule speedMine;

    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(10.0f).setDefaultValue(6.0f).setFormat("m")
            .setDescription("The max range to target players").build();
    Config<Integer> delayConfig = new NumberConfig.Builder<Integer>("Delay")
            .setMin(100).setMax(500).setDefaultValue(200).setFormat("ms")
            .setDescription("The delay between mines").build();
    Config<Boolean> antiCrawl = new BooleanConfig.Builder("AntiCrawl")
            .setDescription("Attempts to mine blocks to prevent player crawl")
            .setDefaultValue(true).build();
    Config<Boolean> feetConfig = new BooleanConfig.Builder("Feet")
            .setDescription("Mines out target feet blocks")
            .setDefaultValue(true).build();
    Config<Boolean> bodyConfig = new BooleanConfig.Builder("Head")
            .setDescription("Mines out target body blocks")
            .setDefaultValue(false).build();
    Config<Boolean> headConfig = new BooleanConfig.Builder("Ceiling")
            .setDescription("Mines out above target head blocks")
            .setDefaultValue(false).build();
    Config<Boolean> floorConfig = new BooleanConfig.Builder("Floor")
            .setDescription("Mines out target floor blocks")
            .setDefaultValue(false).build();
    Config<Boolean> avoidSelf = new BooleanConfig.Builder("AvoidSelf")
            .setDescription("Avoids mining out blocks we are near")
            .setDefaultValue(false).build();
    Config<Void> targetingConfig = new ConfigGroup.Builder("Targeting")
            .addAll(feetConfig, bodyConfig, headConfig, floorConfig, avoidSelf).build();

    private final Timer mineTimer = new NanoTimer();

    private final TrapSpec trapSpec = TrapSpec.builder()
            .layers(EnumSet.of(TrapLayer.FEET,
                    TrapLayer.BODY,
                    TrapLayer.CEILING,
                    TrapLayer.FLOOR,
                    TrapLayer.FEET_INTERSECT,
                    TrapLayer.BODY_INTERSECT)).build();

    public AutoMineModule()
    {
        super("AutoMine", "Mines blocks around enemies", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        speedMine = SpeedMineModule.INSTANCE;
    }

    @EventListener(priority = TickPriorities.AUTO_MINE)
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || !speedMine.isEnabled() || !PlayerUtil.isInSurvival(mc.player))
        {
            Managers.TARGETING.clearTarget();
            return;
        }

        if (speedMine.isManualMining())
        {
            mineTimer.reset();
            return;
        }

        PlayerEntity target = Managers.TARGETING.setClosestTarget(rangeConfig.getValue());
        if (target == null)
        {
            return;
        }

        BlockPos targetPos = EntityUtil.getRoundedBlockPos(target);

        Box boundingBox = target.getBoundingBox();
        long roundedY = Math.round(boundingBox.minY);
        Box bb = boundingBox.withMinY(roundedY).shrink(0.01, 0.1, 0.01);

        BlockPos autoMine = getNextAutoMine(targetPos, bb);

        if (autoMine != null && canStartMining(autoMine) && mineTimer.hasPassed(delayConfig.getValue()))
        {
            startAutoMine(autoMine);
            mineTimer.reset();
        }
    }

    private void startAutoMine(BlockPos blockPos)
    {
        BlockState state = mc.world.getBlockState(blockPos);
        if (MiningUtil.canMineBlock(state) && !speedMine.isMining(blockPos))
        {
            speedMine.startMining(blockPos, Direction.UP);
        }
    }

    public BlockPos getNextAutoMine(BlockPos targetPos, Box boundingBox)
    {
        boolean shouldTargetHead = mc.world.getBlockState(targetPos).isOf(Blocks.BEDROCK);
        float crystalRange = AutoCrystalModule.INSTANCE.getPlaceRange().getValue();

        BlockPos bestMine = null;
        boolean inRange = false;

        trapPos.calcTrap(boundingBox, trapSpec);
        for (Map.Entry<BlockPos, TrapLayer> trapLayer : trapPos.getTrapPositionLayers().entrySet())
        {
            BlockPos blockPos = trapLayer.getKey();
            TrapLayer layer = trapLayer.getValue();

            BlockState state = mc.world.getBlockState(blockPos);
            if (MiningUtil.isUnbreakable(state))
            {
                continue;
            }

            BlockState state2 = mc.world.getBlockState(blockPos.down());
            if (!state2.isOf(Blocks.OBSIDIAN) && !state2.isOf(Blocks.BEDROCK))
            {
                continue;
            }

            double dist = mc.player.squaredDistanceTo(blockPos.toCenterPos());
            if (dist > speedMine.getRangeConfig().getValue() * speedMine.getRangeConfig().getValue())
            {
                continue;
            }

            if (shouldTargetHead)
            {
                if (!speedMine.isMining(blockPos) && !MiningUtil.isEmpty(state))
                {
                    if (layer == TrapLayer.FLOOR && floorConfig.getValue()
                            || layer == TrapLayer.BODY_INTERSECT && bodyConfig.getValue())
                    {
                        return blockPos;
                    }
                }

                if (layer == TrapLayer.BODY && bodyConfig.getValue())
                {
                    if (!inRange)
                    {
                        bestMine = blockPos;
                    }

                    Vec3d crystalVec = blockPos.toBottomCenterPos().add(0.0, 1.0, 0.0);
                    if (mc.player.getEyePos().squaredDistanceTo(crystalVec) <= crystalRange * crystalRange)
                    {
                        inRange = true;
                    }
                }
            } else if (feetConfig.getValue())
            {
                if (layer == TrapLayer.FEET_INTERSECT && !MiningUtil.isEmpty(state) && !speedMine.isMining(blockPos))
                {
                    return blockPos;
                }

                if (layer == TrapLayer.FEET)
                {
                    if (!inRange)
                    {
                        bestMine = blockPos;
                    }

                    Vec3d crystalVec = blockPos.toBottomCenterPos().add(0.0, 1.0, 0.0);
                    if (mc.player.getEyePos().squaredDistanceTo(crystalVec) <= crystalRange * crystalRange)
                    {
                        inRange = true;
                    }
                }
            }
        }

        return bestMine;
    }

    private boolean canStartMining(BlockPos currentMine)
    {
        MiningData main = speedMine.getMainMiningBlock();
        MiningData packet = speedMine.getPacketMiningBlock();

        if (main != null && packet != null)
        {
            BlockPos mainPos = main.getBlockPos();
            BlockPos packetPos = packet.getBlockPos();
            if (mainPos.equals(currentMine) || packetPos.equals(currentMine))
            {
                return false;
            }

            return main.isDoneMining() && (packet.isBlockMined() || packet.hasMinedFor(30));
        } else if (main != null)
        {
            BlockPos mainPos = main.getBlockPos();
            if (!mainPos.equals(currentMine))
            {
                return true;
            }

            return main.isBlockMined();
        }

        return packet == null;
    }
}

package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.ObsidianPlacerModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.world.AirPlaceModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.ExplosionUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasePlaceModule extends ObsidianPlacerModule
{
    Config<Float> placeRangeConfig = register(new NumberConfig<>("PlaceRange", "The placement range for bases", 0.0f, 4.0f, 6.0f));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f));
    // Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 10));
    Config<Float> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0.0f, 1.0f, 5.0f));
    Config<Float> minDamageConfig = register(new NumberConfig<>("MinDamage", "Minimum damage required to place base", 1.0f, 4.0f, 10.0f));
    Config<Boolean> assumeArmorConfig = register(new BooleanConfig("AssumeBestArmor", "Assumes Prot 0 armor is max armor", false));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where base blocks are being placed", true));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));

    private BlockPos crystalBase;
    private final Map<BlockPos, Long> packets = new HashMap<>();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();

    public BasePlaceModule()
    {
        super("BasePlace", "Places obsidian for crystal placements", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable()
    {
        crystalBase = null;
        packets.clear();
        fadeList.clear();
    }

    @EventListener
    public void onTick(PlayerTickEvent event)
    {
        if (!AutoCrystalModule.getInstance().isEnabled() || AutoCrystalModule.getInstance().isPlacing())
        {
            return;
        }

        PlayerEntity target = getClosestPlayer(enemyRangeConfig.getValue());
        if (target == null)
        {
            return;
        }

        crystalBase = getCrystalBase(target);
        if (crystalBase == null)
        {
            return;
        }

        BlockState state = mc.world.getBlockState(crystalBase);
        int slot = getResistantBlockItem();
        if (slot == -1 || !state.isReplaceable())
        {
            return;
        }

        placeBlock(crystalBase, slot);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
                Color boxColor = ColorsModule.getInstance().getColor(boxAlpha);
                Color lineColor = ColorsModule.getInstance().getColor(lineAlpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }
            RenderBuffers.postRender();

            if (crystalBase != null && mc.world.isAir(crystalBase))
            {
                Animation animation = new Animation(true, fadeTimeConfig.getValue());
                fadeList.put(crystalBase, animation);
            }
        }

        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);
    }

    private void placeBlock(BlockPos pos, int slot)
    {
        Managers.INTERACT.placeBlock(pos, slot, strictDirectionConfig.getValue(), false, true, (state, angles) ->
        {
            if (rotateConfig.getValue())
            {
                if (state)
                {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
                }
                else
                {
                    Managers.ROTATION.setRotationSilentSync();
                }
            }
        });
        packets.put(pos, System.currentTimeMillis());
    }

    private BlockPos getCrystalBase(PlayerEntity player)
    {
        List<BlockPos> targetBlocks = getSphere(placeRangeConfig.getValue(), mc.player.getEyePos());
        double damage = 0.0f;
        BlockPos crystalBase = null;
        for (BlockPos pos : targetBlocks)
        {
            final BlockPos basePos = pos.down();
            if (basePos.getY() >= EntityUtil.getRoundedBlockPos(player).getY())
            {
                continue;
            }

            Long placed = packets.get(basePos);
            if (shiftDelayConfig.getValue() > 0.0f && placed != null && System.currentTimeMillis() - placed < shiftDelayConfig.getValue() * 50.0f)
            {
                continue;
            }

            if (!AutoCrystalModule.getInstance().isCrystalHitboxClear(pos))
            {
                continue;
            }

            double dist = mc.player.squaredDistanceTo(basePos.toCenterPos());
            if (dist > ((NumberConfig) placeRangeConfig).getValueSq())
            {
                continue;
            }

            double dmg1 = ExplosionUtil.getDamageTo(player, pos.toCenterPos(), assumeArmorConfig.getValue());
            if (dmg1 < minDamageConfig.getValue())
            {
                continue;
            }

            if (!AirPlaceModule.getInstance().isEnabled()
                    && Managers.INTERACT.getInteractDirectionInternal(basePos, strictDirectionConfig.getValue()) == null)
            {
                continue;
            }

            if (!mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, basePos, ShapeContext.absent()))
            {
                continue;
            }

            if (dmg1 > damage)
            {
                crystalBase = basePos;
                damage = dmg1;
            }
        }

        return crystalBase;
    }

    private List<BlockPos> getSphere(double rad, Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }
}

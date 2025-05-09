package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.manager.combat.hole.Hole;
import net.shoreline.client.impl.manager.combat.hole.HoleType;
import net.shoreline.client.impl.module.ObsidianPlacerModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linus
 * @since 1.0
 */
public class HoleFillModule extends ObsidianPlacerModule
{
    private static HoleFillModule INSTANCE;

    //
    Config<Boolean> obsidianConfig = register(new BooleanConfig("Obsidian", "Fills obsidian holes", true));
    Config<Boolean> doublesConfig = register(new BooleanConfig("Doubles", "Fills double holes", false));
    Config<Float> rangeConfig = register(new NumberConfig<>("PlaceRange", "The range to fill nearby holes", 0.1f, 4.0f, 6.0f));
    Config<Boolean> websConfig = register(new BooleanConfig("Webs", "Fills holes with webs", false));
    Config<Boolean> autoConfig = register(new BooleanConfig("Auto", "Fills holes when enemies are within a certain range", false));
    Config<Float> targetRangeConfig = register(new NumberConfig<>("TargetRange", "The range from the target to the hole", 0.5f, 3.0f, 5.0f, () -> autoConfig.getValue()));
    Config<Float> enemyRangeConfig = register(new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f, () -> autoConfig.getValue()));
    Config<Boolean> attackConfig = register(new BooleanConfig("Attack", "Attacks crystals in the way of hole fill", true));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates to block before placing", false));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5));
    Config<Integer> shiftDelayConfig = register(new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5));
    Config<Boolean> autoDisableConfig = register(new BooleanConfig("AutoDisable", "Disables after filling all holes", false));
    Config<Boolean> renderConfig = register(new BooleanConfig("Render", "Renders where blocks are being filled", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false));
    private int shiftDelay;
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private List<BlockPos> fills = new ArrayList<>();

    /**
     *
     */
    public HoleFillModule()
    {
        super("HoleFill", "Fills in nearby holes with blocks", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static HoleFillModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        fadeList.clear();
        fills.clear();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        disable();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        //
        int blocksPlaced = 0;

        if (!multitaskConfig.getValue() && checkMultitask())
        {
            fills.clear();
            return;
        }

        final int slot = websConfig.getValue() ? getBlockItemSlot(Blocks.COBWEB) : getResistantBlockItem();
        if (slot == -1)
        {
            fills.clear();
            return;
        }

        if (shiftDelayConfig.getValue() > 0 && shiftDelay < shiftDelayConfig.getValue())
        {
            shiftDelay++;
            return;
        }
        List<BlockPos> holes = new ArrayList<>();
        for (Hole hole : Managers.HOLE.getHoles())
        {
            if (hole.isQuad() || hole.isDouble() && !doublesConfig.getValue() || hole.getSafety() == HoleType.OBSIDIAN && !obsidianConfig.getValue())
            {
                continue;
            }
            if (hole.squaredDistanceTo(mc.player) > ((NumberConfig) rangeConfig).getValueSq())
            {
                continue;
            }

            if (!mc.world.canPlace(DEFAULT_OBSIDIAN_STATE, hole.getPos(), ShapeContext.absent()))
            {
                continue;
            }

            if (autoConfig.getValue())
            {
                for (PlayerEntity entity : mc.world.getPlayers())
                {
                    if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName()))
                    {
                        continue;
                    }
                    double dist = mc.player.distanceTo(entity);
                    if (dist > enemyRangeConfig.getValue())
                    {
                        continue;
                    }
                    if (entity.getY() >= hole.getY() &&
                            hole.squaredDistanceTo(entity) > ((NumberConfig) targetRangeConfig).getValueSq())
                    {
                        continue;
                    }
                    holes.add(hole.getPos());
                    break;
                }
            }
            else
            {
                holes.add(hole.getPos());
            }
        }
        fills = holes;
        if (fills.isEmpty())
        {
            if (autoDisableConfig.getValue())
            {
                disable();
            }
            return;
        }
        if (attackConfig.getValue())
        {
            attackBlockingCrystals(fills);
        }
        while (blocksPlaced < shiftTicksConfig.getValue())
        {
            if (blocksPlaced >= fills.size())
            {
                break;
            }
            BlockPos targetPos = fills.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            placeBlock(targetPos, slot);
        }

        if (rotateConfig.getValue())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
    }

    public void attackBlockingCrystals(List<BlockPos> posList)
    {
        for (BlockPos pos : posList)
        {
            Entity crystalEntity = mc.world.getOtherEntities(null, new Box(pos)).stream()
                    .filter(e -> e instanceof EndCrystalEntity).findFirst().orElse(null);
            if (crystalEntity == null)
            {
                continue;
            }
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            return;
        }
    }

    private void placeBlock(BlockPos targetPos, int slot)
    {
        Managers.INTERACT.placeBlock(targetPos, slot, strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue() && state)
            {
                Managers.ROTATION.setRotationSilent(angles[0], angles[1]);
            }
        });
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

            if (fills.isEmpty())
            {
                return;
            }

            for (BlockPos pos : fills)
            {
                Animation animation = new Animation(true, fadeTimeConfig.getValue());
                fadeList.put(pos, animation);
            }
        }

        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);
    }

    public boolean isPlacing()
    {
        return !fills.isEmpty();
    }
}

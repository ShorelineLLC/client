package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
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
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ObsidianPlacerModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.manager.combat.hole.Hole;
import net.shoreline.client.impl.manager.combat.hole.HoleType;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.render.animation.Animation;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linus
 * @since 1.0
 */
public class HoleFillModule extends ObsidianPlacerModule {
    //
    Config<Boolean> obsidianConfig = new BooleanConfig("Obsidian", "Fills obsidian holes", true);
    Config<Boolean> doublesConfig = new BooleanConfig("Doubles", "Fills double holes", false);
    Config<Float> rangeConfig = new NumberConfig<>("PlaceRange", "The range to fill nearby holes", 0.1f, 4.0f, 6.0f);
    Config<Boolean> websConfig = new BooleanConfig("Webs", "Fills holes with webs", false);
    Config<Boolean> autoConfig = new BooleanConfig("Auto", "Fills holes when enemies are within a certain range", false);
    Config<Float> targetRangeConfig = new NumberConfig<>("TargetRange", "The range from the target to the hole", 0.5f, 3.0f, 5.0f, () -> autoConfig.getValue());
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f, () -> autoConfig.getValue());
    Config<Boolean> attackConfig = new BooleanConfig("Attack", "Attacks crystals in the way of hole fill", true);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates to block before placing", false);
    Config<Integer> shiftTicksConfig = new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5);
    Config<Integer> shiftDelayConfig = new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5);
    Config<Boolean> autoDisableConfig = new BooleanConfig("AutoDisable", "Disables after filling all holes", false);
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders where scaffold is placing blocks", false);
    Config<Integer> fadeTimeConfig = new NumberConfig<>("Fade-Time", "Time to fade", 0, 250, 1000, () -> false);
    private int shiftDelay;
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private List<BlockPos> fills = new ArrayList<>();

    /**
     *
     */
    public HoleFillModule() {
        super("HoleFill", "Fills in nearby holes with blocks", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable() {
        fadeList.clear();
        fills.clear();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event) {
        disable();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        //
        int blocksPlaced = 0;
        if (shiftDelay < shiftDelayConfig.getValue()) {
            shiftDelay++;
            return;
        }
        List<BlockPos> holes = new ArrayList<>();
        for (Hole hole : Managers.HOLE.getHoles()) {
            if (hole.isQuad() || hole.isDouble() && !doublesConfig.getValue() || hole.getSafety() == HoleType.OBSIDIAN && !obsidianConfig.getValue()) {
                continue;
            }
            if (hole.squaredDistanceTo(mc.player) > ((NumberConfig) rangeConfig).getValueSq()) {
                continue;
            }
            if (mc.world.getOtherEntities(null, new Box(hole.getPos()))
                    .stream().anyMatch(e -> !Modules.SURROUND.isEntityBlockingSurround(e))) {
                continue;
            }
            if (autoConfig.getValue()) {
                for (PlayerEntity entity : mc.world.getPlayers()) {
                    if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                        continue;
                    }
                    double dist = mc.player.distanceTo(entity);
                    if (dist > enemyRangeConfig.getValue()) {
                        continue;
                    }
                    if (entity.getY() >= hole.getY() &&
                            hole.squaredDistanceTo(entity) > ((NumberConfig) targetRangeConfig).getValueSq()) {
                        continue;
                    }
                    holes.add(hole.getPos());
                    break;
                }
            } else {
                holes.add(hole.getPos());
            }
        }
        fills = holes;
        if (fills.isEmpty()) {
            if (autoDisableConfig.getValue()) {
                disable();
            }
            return;
        }
        while (blocksPlaced < shiftTicksConfig.getValue()) {
            if (blocksPlaced >= fills.size()) {
                break;
            }
            BlockPos targetPos = fills.get(blocksPlaced);
            blocksPlaced++;
            shiftDelay = 0;
            // All rotations for shift ticks must send extra packet
            // This may not work on all servers
            attackPlace(targetPos);
        }
    }

    private void attack(Entity entity) {
        Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void attackPlace(BlockPos targetPos)
    {
        final int slot = websConfig.getValue() ? getBlockItemSlot(Blocks.COBWEB) : getResistantBlockItem();
        if (slot == -1)
        {
            return;
        }
        attackPlace(targetPos, slot);
    }

    private void attackPlace(BlockPos targetPos, int slot) {
        if (attackConfig.getValue()) {
            List<Entity> entities = mc.world.getOtherEntities(null, new Box(targetPos)).stream().filter(e -> e instanceof EndCrystalEntity).toList();
            for (Entity entity : entities) {
                attack(entity);
            }
        }

        Managers.INTERACT.placeBlock(targetPos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue())
            {
                if (state)
                {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                }
                else
                {
                    Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                }
            }
        });
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                set.getValue().setState(false);
                int boxAlpha = (int) (80 * set.getValue().getFactor());
                int lineAlpha = (int) (145 * set.getValue().getFactor());
                Color boxColor = Modules.COLORS.getColor(boxAlpha);
                Color lineColor = Modules.COLORS.getColor(lineAlpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }


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
}

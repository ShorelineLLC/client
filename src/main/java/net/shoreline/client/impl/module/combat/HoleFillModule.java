package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ObsidianPlacerModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.manager.combat.hole.Hole;
import net.shoreline.client.init.Managers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class HoleFillModule extends ObsidianPlacerModule {
    //
    Config<Float> rangeConfig = new NumberConfig<>("PlaceRange", "The range to fill nearby holes", 0.1f, 4.0f, 6.0f);
    Config<Boolean> proximityConfig = new BooleanConfig("ProximityCheck", "Fills holes when enemies are within a certain range", false);
    Config<Float> proximityRangeConfig = new NumberConfig<>("Range", "The range from the target to the hole", 0.5f, 1.0f, 4.0f, () -> proximityConfig.getValue());
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "The maximum range of targets", 0.1f, 10.0f, 15.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates to block before placing", false);
    Config<Integer> shiftTicksConfig = new NumberConfig<>("ShiftTicks", "The number of blocks to place per tick", 1, 2, 5);
    Config<Integer> shiftDelayConfig = new NumberConfig<>("ShiftDelay", "The delay between each block placement interval", 0, 1, 5);
    Config<Boolean> autoDisableConfig = new BooleanConfig("AutoDisable", "Disables after filling all holes", false);
    private int shiftDelay;

    /**
     *
     */
    public HoleFillModule() {
        super("HoleFill", "Fills in nearby holes with blocks", ModuleCategory.COMBAT);
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
        List<BlockPos> fills = new ArrayList<>();
        for (Hole hole : Managers.HOLE.getHoles()) {
            if (hole.isQuad()) {
                continue;
            }
            if (hole.squaredDistanceTo(mc.player) > ((NumberConfig) rangeConfig).getValueSq()) {
                continue;
            }
            if (proximityConfig.getValue()) {
                for (Entity entity : mc.world.getEntities()) {
                    if (!(entity instanceof PlayerEntity player) || player == mc.player) {
                        continue;
                    }
                    double dist = mc.player.distanceTo(player);
                    if (dist > enemyRangeConfig.getValue()) {
                        continue;
                    }
                    if (player.getY() > hole.getY() &&
                            hole.squaredDistanceTo(player) > ((NumberConfig) proximityRangeConfig).getValueSq()) {
                        continue;
                    }
                    fills.add(hole.getPos());
                    break;
                }
            } else {
                fills.add(hole.getPos());
            }
        }
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
//            blockInteraction = placeBlockResistant(targetPos, strictDirectionConfig.getValue());
//            if (rotateConfig.getValue() && blockInteraction != null) {
//                float[] rotations = blockInteraction.rotations();
//                setRotation(rotations[0], rotations[1]);
//            }
        }
    }
}

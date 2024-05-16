package net.shoreline.client.impl.module.client;

import baritone.api.BaritoneAPI;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.TickEvent;

import java.awt.*;

/**
 * @author Shoreline
 * @since 1.0
 */
public class BaritoneModule extends ConcurrentModule {

    Config<Float> rangeConfig = new NumberConfig<>("Range", "Baritone block reach distance", 1.0f, 4.0f, 5.0f);
    Config<Boolean> placeConfig = new BooleanConfig("Place", "Allow baritone to place blocks", true);
    Config<Boolean> breakConfig = new BooleanConfig("Break", "Allow baritone to break blocks", true);
    Config<Boolean> sprintConfig = new BooleanConfig("Sprint", "Allow baritone to sprint", true);
    Config<Boolean> inventoryConfig = new BooleanConfig("UseInventory", "Allow baritone to use player inventory", false);
    Config<Boolean> vinesConfig = new BooleanConfig("Vines", "Allow baritone to climb vines", true);
    Config<Boolean> jump256Config = new BooleanConfig("JumpAt256", "Allow baritone to jump at 256 blocks", false);
    Config<Boolean> waterBucketFallConfig = new BooleanConfig("WaterBucketFall", "Allow baritone to use waterbuckets when falling", false);
    Config<Boolean> parkourConfig = new BooleanConfig("Parkour", "Allow baritone to jump between blocks", true);
    Config<Boolean> parkourPlaceConfig = new BooleanConfig("ParkourPlace", "Allow baritone to jump and place blocks", false);
    Config<Boolean> parkourAscendConfig = new BooleanConfig("ParkourAscend", "Allow baritone to jump up blocks", true);
    Config<Boolean> diagonalAscendConfig = new BooleanConfig("DiagonalAscend", "Allow baritone to jump up blocks diagonally", false);
    Config<Boolean> diagonalDescendConfig = new BooleanConfig("DiagonalDescend", "Allow baritone to move down blocks diagonally", false);
    Config<Boolean> mineDownConfig = new BooleanConfig("MineDownward", "Allow baritone to mine down", true);
    Config<Boolean> legitMineConfig = new BooleanConfig("LegitMine", "Uses baritone legit mine", false);
    Config<Boolean> logOnArrivalConfig = new BooleanConfig("LogOnArrival", "Logout when you arrive at destination", false);
    Config<Boolean> freeLookConfig = new BooleanConfig("FreeLook", "Allows you to look around freely while using baritone", true);
    Config<Boolean> antiCheatConfig = new BooleanConfig("AntiCheat", "Uses NCP placements and breaks", false);
    Config<Boolean> strictLiquidConfig = new BooleanConfig("Strict-Liquid", "Uses strick liquid checks", false);
    Config<Boolean> censorCoordsConfig = new BooleanConfig("CensorCoords", "Censors goal coordinates in chat", false);
    Config<Boolean> censorCommandsConfig = new BooleanConfig("CensorCommands", "Censors baritone commands in chat", false);
    Config<Boolean> debugConfig = new BooleanConfig("Debug", "Debugs in the chat", false);
    Config<Color> goalColor = new ColorConfig("GoalColor", "The color of the goal box", Color.GREEN, false, false);
    Config<Color> pathColor = new ColorConfig("CurrentPathColor", "The color of the path", Color.RED, false, false);
    Config<Color> nextPathColor = new ColorConfig("NextPathColor", "The color of the path", Color.MAGENTA, false, false);


    /**
     *
     */
    public BaritoneModule() {
        super("Baritone", "Configure baritone", ModuleCategory.CLIENT);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.POST) {
            return;
        }
        BaritoneAPI.getSettings().blockReachDistance.value = rangeConfig.getValue();
        BaritoneAPI.getSettings().allowPlace.value = placeConfig.getValue();
        BaritoneAPI.getSettings().allowBreak.value = breakConfig.getValue();
        BaritoneAPI.getSettings().allowSprint.value = sprintConfig.getValue();
        BaritoneAPI.getSettings().allowInventory.value = inventoryConfig.getValue();
        BaritoneAPI.getSettings().allowVines.value = vinesConfig.getValue();
        BaritoneAPI.getSettings().allowJumpAt256.value = jump256Config.getValue();
        BaritoneAPI.getSettings().allowWaterBucketFall.value = waterBucketFallConfig.getValue();
        BaritoneAPI.getSettings().allowParkour.value = parkourConfig.getValue();
        BaritoneAPI.getSettings().allowParkourAscend.value = parkourAscendConfig.getValue();
        BaritoneAPI.getSettings().allowParkourPlace.value = parkourPlaceConfig.getValue();
        BaritoneAPI.getSettings().allowDiagonalAscend.value = diagonalAscendConfig.getValue();
        BaritoneAPI.getSettings().allowDiagonalDescend.value = diagonalDescendConfig.getValue();
        BaritoneAPI.getSettings().allowDownward.value = mineDownConfig.getValue();
        BaritoneAPI.getSettings().legitMine.value = legitMineConfig.getValue();
        BaritoneAPI.getSettings().disconnectOnArrival.value = logOnArrivalConfig.getValue();
        BaritoneAPI.getSettings().freeLook.value = freeLookConfig.getValue();
        BaritoneAPI.getSettings().antiCheatCompatibility.value = antiCheatConfig.getValue();
        BaritoneAPI.getSettings().strictLiquidCheck.value = strictLiquidConfig.getValue();
        BaritoneAPI.getSettings().censorCoordinates.value = censorCoordsConfig.getValue();
        BaritoneAPI.getSettings().censorRanCommands.value = censorCommandsConfig.getValue();
        BaritoneAPI.getSettings().chatDebug.value = debugConfig.getValue();
        BaritoneAPI.getSettings().colorGoalBox.value = goalColor.getValue();
        BaritoneAPI.getSettings().colorCurrentPath.value = pathColor.getValue();
        BaritoneAPI.getSettings().colorMostRecentConsidered.value = pathColor.getValue();
        BaritoneAPI.getSettings().colorNextPath.value = nextPathColor.getValue();
    }
}

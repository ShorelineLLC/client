package net.shoreline.client.impl.module.client;

import baritone.api.BaritoneAPI;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.impl.event.world.LoadWorldEvent;
import net.shoreline.client.util.FormattingUtil;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.awt.*;

/**
 * @author Shoreline
 * @since 1.0
 */
public class BaritoneModule extends ConcurrentModule
{

    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "Baritone block reach distance", 0.0f, 4.0f, 5.0f));
    Config<Boolean> placeConfig = register(new BooleanConfig("Place", "Allow baritone to place blocks", true));
    Config<Boolean> breakConfig = register(new BooleanConfig("Break", "Allow baritone to break blocks", true));
    Config<Boolean> sprintConfig = register(new BooleanConfig("Sprint", "Allow baritone to sprint", true));
    Config<Boolean> inventoryConfig = register(new BooleanConfig("UseInventory", "Allow baritone to use player inventory", false));
    Config<Boolean> vinesConfig = register(new BooleanConfig("Vines", "Allow baritone to climb vines", true));
    Config<Boolean> jump256Config = register(new BooleanConfig("JumpAt256", "Allow baritone to jump at 256 blocks", false));
    Config<Boolean> waterBucketFallConfig = register(new BooleanConfig("WaterBucketFall", "Allow baritone to use waterbuckets when falling", false));
    Config<Boolean> parkourConfig = register(new BooleanConfig("Parkour", "Allow baritone to jump between blocks", true));
    Config<Boolean> parkourPlaceConfig = register(new BooleanConfig("ParkourPlace", "Allow baritone to jump and place blocks", false));
    Config<Boolean> parkourAscendConfig = register(new BooleanConfig("ParkourAscend", "Allow baritone to jump up blocks", true));
    Config<Boolean> diagonalAscendConfig = register(new BooleanConfig("DiagonalAscend", "Allow baritone to jump up blocks diagonally", false));
    Config<Boolean> diagonalDescendConfig = register(new BooleanConfig("DiagonalDescend", "Allow baritone to move down blocks diagonally", false));
    Config<Boolean> mineDownConfig = register(new BooleanConfig("MineDownward", "Allow baritone to mine down", true));
    Config<Boolean> legitMineConfig = register(new BooleanConfig("LegitMine", "Uses baritone legit mine", false));
    Config<Boolean> logOnArrivalConfig = register(new BooleanConfig("LogOnArrival", "Logout when you arrive at destination", false));
    Config<Boolean> freeLookConfig = register(new BooleanConfig("FreeLook", "Allows you to look around freely while using baritone", true));
    Config<Boolean> antiCheatConfig = register(new BooleanConfig("AntiCheat", "Uses NCP placements and breaks", false));
    Config<Boolean> strictLiquidConfig = register(new BooleanConfig("Strict-Liquid", "Uses strick liquid checks", false));
    Config<Boolean> censorCoordsConfig = register(new BooleanConfig("CensorCoords", "Censors goal coordinates in chat", false));
    Config<Boolean> censorCommandsConfig = register(new BooleanConfig("CensorCommands", "Censors baritone commands in chat", false));
    Config<Boolean> chatControlConfig = register(new BooleanConfig("ChatControl", "Allows you to type baritone commands in chat without prefix", true));
    Config<Boolean> debugConfig = register(new BooleanConfig("Debug", "Debugs in the chat", false));
    Config<Color> goalColor = register(new ColorConfig("GoalColor", "The color of the goal box", Color.GREEN, false, false));
    Config<Color> pathColor = register(new ColorConfig("CurrentPathColor", "The color of the path", Color.RED, false, false));
    Config<Color> nextPathColor = register(new ColorConfig("NextPathColor", "The color of the path", Color.MAGENTA, false, false));

    /**
     *
     */
    public BaritoneModule()
    {
        super("Baritone", "Configure baritone", ModuleCategory.CLIENT);
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.POST || event.getConfig().getContainer() == this)
        {
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
        BaritoneAPI.getSettings().chatControl.value = chatControlConfig.getValue();
        BaritoneAPI.getSettings().chatDebug.value = debugConfig.getValue();
        BaritoneAPI.getSettings().colorGoalBox.value = goalColor.getValue();
        BaritoneAPI.getSettings().colorCurrentPath.value = pathColor.getValue();
        BaritoneAPI.getSettings().colorMostRecentConsidered.value = pathColor.getValue();
        BaritoneAPI.getSettings().colorNextPath.value = nextPathColor.getValue();
    }
    
    @EventListener
    public void onChat(ChatMessageEvent event)
    {
        syncBaritoneSettings();
    }

    @EventListener
    public void onLoadWorld(LoadWorldEvent event)
    {
        syncBaritoneSettings();
    }

    private void syncBaritoneSettings()
    {
        rangeConfig.setValue(BaritoneAPI.getSettings().blockReachDistance.value);
        placeConfig.setValue(BaritoneAPI.getSettings().allowPlace.value);
        breakConfig.setValue(BaritoneAPI.getSettings().allowBreak.value);
        sprintConfig.setValue(BaritoneAPI.getSettings().allowSprint.value);
        inventoryConfig.setValue(BaritoneAPI.getSettings().allowInventory.value);
        vinesConfig.setValue(BaritoneAPI.getSettings().allowVines.value);
        jump256Config.setValue(BaritoneAPI.getSettings().allowJumpAt256.value);
        waterBucketFallConfig.setValue(BaritoneAPI.getSettings().allowWaterBucketFall.value);
        parkourConfig.setValue(BaritoneAPI.getSettings().allowParkour.value);
        parkourAscendConfig.setValue(BaritoneAPI.getSettings().allowParkourAscend.value);
        parkourPlaceConfig.setValue(BaritoneAPI.getSettings().allowParkourPlace.value);
        diagonalAscendConfig.setValue(BaritoneAPI.getSettings().allowDiagonalAscend.value);
        diagonalDescendConfig.setValue(BaritoneAPI.getSettings().allowDiagonalDescend.value);
        mineDownConfig.setValue(BaritoneAPI.getSettings().allowDownward.value);
        legitMineConfig.setValue(BaritoneAPI.getSettings().legitMine.value);
        logOnArrivalConfig.setValue(BaritoneAPI.getSettings().disconnectOnArrival.value);
        freeLookConfig.setValue(BaritoneAPI.getSettings().freeLook.value);
        antiCheatConfig.setValue(BaritoneAPI.getSettings().antiCheatCompatibility.value);
        strictLiquidConfig.setValue(BaritoneAPI.getSettings().strictLiquidCheck.value);
        censorCoordsConfig.setValue(BaritoneAPI.getSettings().censorCoordinates.value);
        censorCommandsConfig.setValue(BaritoneAPI.getSettings().censorRanCommands.value);
        chatControlConfig.setValue(BaritoneAPI.getSettings().chatControl.value);
        debugConfig.setValue(BaritoneAPI.getSettings().chatDebug.value);
        goalColor.setValue(BaritoneAPI.getSettings().colorGoalBox.value);
        pathColor.setValue(BaritoneAPI.getSettings().colorCurrentPath.value);
        pathColor.setValue(BaritoneAPI.getSettings().colorMostRecentConsidered.value);
        nextPathColor.setValue(BaritoneAPI.getSettings().colorNextPath.value);
    }

    @EventListener
    public void onChatText(ChatMessageEvent event)
    {
        String text = event.getText().getString();
        if (text.startsWith("[Baritone]"))
        {
            event.cancel();
            event.setText(Text.of(ChatUtil.PREFIX + FormattingUtil.toString(event.getText())));
        }
    }
}

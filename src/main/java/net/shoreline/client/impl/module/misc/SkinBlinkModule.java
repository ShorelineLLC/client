package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.player.PlayerModelPart;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.mixin.accessor.AccessorGameOptions;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.util.Set;

public class SkinBlinkModule extends ToggleModule
{
    //
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The speed to toggle the player model parts", 0.0f, 0.1f, 20.0f));
    Config<Boolean> randomConfig = register(new BooleanConfig("Random", "Randomizes the toggling of each skin model part", false));
    //
    private final Timer blinkTimer = new CacheTimer();
    // The game option parts
    private Set<PlayerModelPart> enabledPlayerModelParts;

    /**
     *
     */
    public SkinBlinkModule()
    {
        super("SkinBlink", "Toggles the skin model rendering", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (mc.options == null)
        {
            return;
        }
        enabledPlayerModelParts = ((AccessorGameOptions) mc.options).getPlayerModelParts();
    }

    @Override
    public void onDisable()
    {
        if (enabledPlayerModelParts == null || mc.options == null)
        {
            return;
        }
        for (PlayerModelPart modelPart : PlayerModelPart.values())
        {
            mc.options.togglePlayerModelPart(modelPart, enabledPlayerModelParts.contains(modelPart));
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.POST &&
                blinkTimer.passed(speedConfig.getValue() * 1000.0f))
        {
            Set<PlayerModelPart> currentModelParts = ((AccessorGameOptions) mc.options).getPlayerModelParts();
            for (PlayerModelPart modelPart : PlayerModelPart.values())
            {
                mc.options.togglePlayerModelPart(modelPart, randomConfig.getValue() ?
                        Math.random() < 0.5 : !currentModelParts.contains(modelPart));
            }
            blinkTimer.reset();
        }
    }
}

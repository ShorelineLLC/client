package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.player.PlayerModelPart;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.imixin.IGameOptions;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Set;

public class SkinBlinkModule extends Toggleable
{
    Config<Integer> blinkDelay = new NumberConfig.Builder<Integer>("Delay")
            .setMin(100).setMax(2000).setDefaultValue(1000).setFormat("ms")
            .setDescription("The delay between toggling parts").build();

    private final Timer blinkTimer = new NanoTimer();

    private Set<PlayerModelPart> enabledPlayerModelParts;

    public SkinBlinkModule()
    {
        super("SkinBlink", "Toggles skin model parts", GuiCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (mc.options == null)
        {
            return;
        }

        enabledPlayerModelParts = ((IGameOptions) mc.options).getPlayerModelParts();
    }

    @Override
    public void onDisable()
    {
        if (enabledPlayerModelParts == null || mc.options == null || mc.player == null)
        {
            return;
        }

        for (PlayerModelPart modelPart : PlayerModelPart.values())
        {
            mc.options.setPlayerModelPart(modelPart, enabledPlayerModelParts.contains(modelPart));
        }

        mc.player.networkHandler.syncOptions(mc.options.getSyncedOptions());
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (blinkTimer.hasPassed(blinkDelay.getValue()))
        {
            Set<PlayerModelPart> currentModelParts = ((IGameOptions) mc.options).getPlayerModelParts();
            for (PlayerModelPart modelPart : PlayerModelPart.values())
            {
                mc.options.setPlayerModelPart(modelPart, !currentModelParts.contains(modelPart));
            }

            mc.player.networkHandler.syncOptions(mc.options.getSyncedOptions());
            blinkTimer.reset();
        }
    }
}

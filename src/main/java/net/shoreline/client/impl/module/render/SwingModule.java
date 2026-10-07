package net.shoreline.client.impl.module.render;

import net.minecraft.util.Hand;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.entity.HandSwingDurationEvent;
import net.shoreline.client.impl.event.network.SwingHandEvent;
import net.shoreline.client.impl.event.render.item.SwingAnimFactorEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class SwingModule extends Toggleable
{
    public static SwingModule INSTANCE;

    Config<Hand> swingHand = new EnumConfig.Builder<Hand>("Hand")
            .setValues(Hand.values())
            .setDescription("The swinging hand")
            .setDefaultValue(Hand.MAIN_HAND).build();
    Config<Integer> swingSpeed = new NumberConfig.Builder<Integer>("Speed")
            .setMin(0).setMax(20).setDefaultValue(20)
            .setDescription("The speed factor of swinging your hand").build();
    Config<Boolean> oldSwingAnim = new BooleanConfig.Builder("OldSwingAnim")
            .setDescription("Uses the old hand swinging animations")
            .setDefaultValue(false).build();

    private Hand prevHand;

    public SwingModule()
    {
        super("Swing", "Change the swing animation", GuiCategory.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (mc.player != null)
        {
            prevHand = mc.player.preferredHand;
        }
    }

    @Override
    public void onDisable()
    {
        if (mc.player != null)
        {
            mc.player.preferredHand = prevHand;
        }
    }

    @EventListener
    public void onSwingHand(SwingHandEvent event)
    {
        mc.player.preferredHand = getSwingHand();
    }

    @EventListener
    public void onSwingDuration(HandSwingDurationEvent event)
    {
        event.cancel();
        event.setSwingDuration(Math.max(2, 20 - swingSpeed.getValue()));
    }

    @EventListener
    public void onSwingAnimFactor(SwingAnimFactorEvent event)
    {
        if (oldSwingAnim.getValue())
        {
            event.cancel();
        }
    }

    public Hand getSwingHand()
    {
        return swingHand.getValue();
    }
}

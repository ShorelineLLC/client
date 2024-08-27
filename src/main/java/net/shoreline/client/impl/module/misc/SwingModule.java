package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.SwingEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class SwingModule extends ToggleModule
{
    private static SwingModule INSTANCE;

    //
    Config<SwingHand> swingHandConfig = register(new EnumConfig<>("Hand", "The player swinging hand", SwingHand.MAINHAND, SwingHand.values()));
    private Hand prevPreferredHand;
    private Hand hand;

    public SwingModule()
    {
        super("Swing", "Modifies the swinging hand", ModuleCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    public static SwingModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (mc.player != null)
        {
            prevPreferredHand = mc.player.preferredHand;
            if (swingHandConfig.getValue() != SwingHand.PACKET)
            {
                updateSwingHand(swingHandConfig.getValue());
            }
        }
    }

    @Override
    public void onDisable()
    {
        if (mc.player != null)
        {
            mc.player.preferredHand = prevPreferredHand;
        }
    }

    @EventListener
    public void onSwing(SwingEvent event)
    {
        updateSwingHand(swingHandConfig.getValue());
    }

    private void updateSwingHand(SwingHand swingHand)
    {
        hand = switch (swingHand)
        {
            case MAINHAND -> Hand.MAIN_HAND;
            case OFFHAND -> Hand.OFF_HAND;
            case SWAP ->
            {
                if (!mc.player.handSwinging
                        || mc.player.handSwingTicks >= getHandSwingDuration() / 2
                        || mc.player.handSwingTicks < 0)
                {
                    yield hand != Hand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND;
                }
                yield hand;
            }
            case PACKET ->
            {
                mc.player.handSwingTicks = 0;
                mc.player.handSwinging = false;
                yield null;
            }
        };
        if (hand != null)
        {
            mc.player.preferredHand = hand;
        }
    }

    public int getHandSwingDuration()
    {
        if (StatusEffectUtil.hasHaste(mc.player)) {
            return 6 - (1 + StatusEffectUtil.getHasteAmplifier(mc.player));
        }
        return mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) ?
                6 + (1 + mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
    }

    public Hand getSwingHand()
    {
        return hand;
    }

    public Hand getPrevPreferredHand()
    {
        return prevPreferredHand;
    }

    private enum SwingHand
    {
        MAINHAND,
        OFFHAND,
        SWAP,
        PACKET
    }
}
package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.LimbAnimatorEvent;
import net.shoreline.client.impl.event.entity.SwingSpeedEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author hockeyl8
 * @since 1.0
 */
public final class AnimationsModule extends ToggleModule
{
    Config<Boolean> swingSpeedConfig = register(new BooleanConfig("SwingSpeed", "Allows you to modify your swing speed.", true));
    Config<Integer> swingSpeedFactorConfig = register(new NumberConfig<>("SwingSpeed-Factor", "The speed of your swing.", 1, 6, 20, () -> swingSpeedConfig.getValue()));
    Config<Boolean> noLimbSwing = register(new BooleanConfig("NoLimbSwing", "Allows you to cancel limb swing animations", false));

    public AnimationsModule()
    {
        super("Animations", "Allows you to modify vanilla animation mechanics.", ModuleCategory.RENDER);
    }

    @EventListener
    public void onLimbAnimator(LimbAnimatorEvent event)
    {
        if (noLimbSwing.getValue())
        {
            event.cancel();
            event.setSpeed(0.0f);
        }
    }

    @EventListener
    public void onSwingSpeed(SwingSpeedEvent event)
    {
        if (swingSpeedConfig.getValue())
        {
            event.cancel();
            event.setSwingSpeed(swingSpeedFactorConfig.getValue());
        }
    }
}
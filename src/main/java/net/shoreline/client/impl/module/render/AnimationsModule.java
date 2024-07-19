package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.LimbAnimationEvent;
import net.shoreline.client.impl.event.entity.SwingSpeedEvent;
import net.shoreline.client.impl.event.entity.UpdateServerPositionEvent;
import net.shoreline.client.impl.event.render.item.EatTransformationEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author hockeyl8
 * @since 1.0
 */
public final class AnimationsModule extends ToggleModule
{
    Config<Boolean> swingSpeedConfig = register(new BooleanConfig("SwingSpeed", "Allows you to modify your swing speed.", false));
    Config<Integer> swingFactorConfig = register(new NumberConfig<>("SwingFactor", "The speed of your swing.", 1, 6, 20, () -> swingSpeedConfig.getValue()));
    Config<Boolean> selfOnlyConfig = register(new BooleanConfig("SelfOnly", "Make the module only affect yourself", true, () -> false));
    Config<Boolean> eatTransformConfig = register(new BooleanConfig("EatTransform", "Transforms the first person eating animation", false));
    Config<Float> eatTransformFactorConfig = register(new NumberConfig<>("EatTransform-Factor", "Factor for the first person eating animation", 0.0f, 1.0f, 1.0f, () -> eatTransformConfig.getValue()));
    Config<Boolean> limbSwing = register(new BooleanConfig("NoLimbSwing", "Allows you to cancel limb swing animations", false));
    Config<Boolean> interpolationConfig = register(new BooleanConfig("NoInterpolation", "Entities will be rendered at their server positions", false, () -> limbSwing.getValue()));

    public AnimationsModule()
    {
        super("Animations", "Allows you to modify vanilla animations", ModuleCategory.RENDER);
    }

    @EventListener
    public void onSwingSpeed(SwingSpeedEvent event)
    {
        if (swingSpeedConfig.getValue())
        {
            event.cancel();
            event.setSwingSpeed(swingFactorConfig.getValue());
            event.setSelfOnly(selfOnlyConfig.getValue());
        }
    }

    @EventListener
    public void onEatTransformation(EatTransformationEvent event)
    {
        if (eatTransformConfig.getValue())
        {
            event.cancel();
            event.setFactor(eatTransformFactorConfig.getValue());
        }
    }

    @EventListener
    public void onLimbAnimation(LimbAnimationEvent event)
    {
        if (limbSwing.getValue())
        {
            event.cancel();
            event.setSpeed(0.0f);
        }
    }

    @EventListener
    public void onUpdateServerPosition(UpdateServerPositionEvent event)
    {
        if (limbSwing.getValue() && interpolationConfig.getValue())
        {
            event.getLivingEntity().setPos(event.getX(), event.getY(), event.getZ());
            event.getLivingEntity().setYaw(event.getYaw());
            event.getLivingEntity().setPitch(event.getPitch());
        }
    }
}
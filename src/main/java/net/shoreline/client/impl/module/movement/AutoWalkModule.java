package net.shoreline.client.impl.module.movement;

import baritone.api.BaritoneAPI;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class AutoWalkModule extends ToggleModule
{
    private static AutoWalkModule INSTANCE;

    Config<Boolean> lockConfig = register(new BooleanConfig("Lock", "Stops movement when sneaking or jumping", false));

    public AutoWalkModule()
    {
        super("AutoWalk", "Automatically moves forward", ModuleCategory.MOVEMENT);
        INSTANCE = this;
    }

    public static AutoWalkModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        mc.options.forwardKey.setPressed(false);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE)
        {
            if (ShorelineMod.isBaritonePresent() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
            {
                return;
            }

            mc.options.forwardKey.setPressed(!mc.options.sneakKey.isPressed()
                    && (!lockConfig.getValue() || (!mc.options.jumpKey.isPressed() && mc.player.isOnGround())));
        }
    }
}

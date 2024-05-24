package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.eventbus.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;

/**
 * @author linus
 * @since 1.0
 */
public class AutoWalkModule extends ToggleModule {
    //
    Config<Boolean> lockConfig = register(new BooleanConfig("Lock", "Stops movement when sneaking or jumping", false));

    /**
     *
     */
    public AutoWalkModule() {
        super("AutoWalk", "Automatically moves forward", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        mc.options.forwardKey.setPressed(false);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == StageEvent.EventStage.PRE) {
            mc.options.forwardKey.setPressed(!mc.options.sneakKey.isPressed()
                    && (!lockConfig.getValue() || (!mc.options.jumpKey.isPressed() && mc.player.isOnGround())));
        }
    }
}

package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.AttackCooldownEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoHitDelayModule extends ToggleModule
{
    public NoHitDelayModule()
    {
        super("NoHitDelay", "Removes vanilla attack delay", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onAttackCooldown(AttackCooldownEvent event)
    {
        event.cancel();
    }
}

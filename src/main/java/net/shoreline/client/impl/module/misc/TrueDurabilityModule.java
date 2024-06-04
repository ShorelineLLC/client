package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.item.DurabilityEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class TrueDurabilityModule extends ToggleModule
{

    /**
     *
     */
    public TrueDurabilityModule()
    {
        super("TrueDurability", "Displays the true durability of unbreakable items",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onDurability(DurabilityEvent event)
    {
        // ??? Whats this
        int dura = event.getItemDamage();
        if (event.getDamage() < 0)
        {
            dura = event.getDamage();
        }
        event.cancel();
        event.setDamage(dura);
    }
}

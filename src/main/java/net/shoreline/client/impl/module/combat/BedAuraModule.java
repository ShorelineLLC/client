package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

/**
 * @author linus
 * @since 1.0
 */
public class BedAuraModule extends ToggleModule {
    /**
     *
     */
    public BedAuraModule() {
        super("BedAura", "Automatically places and explodes beds",
                ModuleCategory.COMBAT);
    }
}

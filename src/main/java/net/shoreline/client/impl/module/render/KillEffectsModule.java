package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

public class KillEffectsModule extends ToggleModule {

    public KillEffectsModule() {
        super("KillEffects", "Adds effects to player deaths", ModuleCategory.RENDER);
    }

    private enum KillEffect {
        THUNDER,
        FIREWORK
    }
}

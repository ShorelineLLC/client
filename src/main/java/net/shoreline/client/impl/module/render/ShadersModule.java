package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

/**
 * @author linus
 * @since 1.0
 */
public class ShadersModule extends ToggleModule {

    public ShadersModule() {
        super("Shaders", "Renders shaders over entities", ModuleCategory.RENDER);
    }
}

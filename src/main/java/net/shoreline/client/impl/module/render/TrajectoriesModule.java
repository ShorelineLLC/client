package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

/**
 * @author linus
 * @since 1.0
 */
public class TrajectoriesModule extends ToggleModule
{

    public TrajectoriesModule()
    {
        super("Trajectories", "Renders the trajectory path of projectiles", ModuleCategory.RENDER);
    }
}

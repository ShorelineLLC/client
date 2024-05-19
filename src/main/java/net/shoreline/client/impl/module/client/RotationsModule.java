package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;

/**
 * @author linus
 * @since 1.0
 */
public class RotationsModule extends ConcurrentModule {
    public static RotationsModule INSTANCE;

    //
    Config<Float> preserveTicksConfig = new NumberConfig<>("PreserveTicks", "Time to preserve rotations after reaching the target rotations", 0.0f, 10.0f, 20.0f);
    Config<Boolean> movementFixConfig = new BooleanConfig("MovementFix", "Fixes movement on Grim when rotating", false);
    //
    private float prevYaw;

    /**
     *
     */
    public RotationsModule() {
        super("Rotations", "Manages client rotations",
                ModuleCategory.CLIENT);
    }

    public boolean getMovementFix() {
        return movementFixConfig.getValue();
    }

    /**
     * @return
     */
    public float getPreserveTicks() {
        return preserveTicksConfig.getValue();
    }
}

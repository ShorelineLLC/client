package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.module.exploit.DisablerModule;

/**
 * @author linus
 * @since 1.0
 */
public class RotationsModule extends ConcurrentModule
{
    private static RotationsModule INSTANCE;

    //
    Config<Float> preserveTicksConfig = register(new NumberConfig<>("PreserveTicks", "Time to preserve rotations after reaching the target rotations", 0.0f, 10.0f, 20.0f));
    Config<Boolean> movementFixConfig = register(new BooleanConfig("MovementFix", "Fixes movement on Grim when rotating", false));
    Config<Boolean> mouseSensFixConfig = register(new BooleanConfig("MouseSensFix", "Fixes movement on Grim when applying mouse sensitivity", false));
    //
    private float prevYaw;

    /**
     *
     */
    public RotationsModule()
    {
        super("Rotations", "Manages client rotations", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static RotationsModule getInstance()
    {
        return INSTANCE;
    }

    public boolean getMovementFix()
    {
        return movementFixConfig.getValue() && !(DisablerModule.getInstance().isEnabled() && DisablerModule.getInstance().isYawOverflow());
    }

    public boolean getMouseSensFix()
    {
        return mouseSensFixConfig.getValue();
    }

    /**
     * @return
     */
    public float getPreserveTicks()
    {
        return preserveTicksConfig.getValue();
    }
}

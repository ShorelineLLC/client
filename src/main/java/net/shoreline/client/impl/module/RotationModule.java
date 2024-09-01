package net.shoreline.client.impl.module;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.manager.player.rotation.Rotation;
import net.shoreline.client.init.Managers;

/**
 * @author linus
 * @see net.shoreline.client.impl.manager.player.rotation.RotationManager
 * @since 1.0
 */
public class RotationModule extends ToggleModule
{

    private final int rotationPriority;

    /**
     * @param name     The module unique identifier
     * @param desc     The module description
     * @param category The module category
     */
    public RotationModule(String name, String desc, ModuleCategory category)
    {
        super(name, desc, category);
        this.rotationPriority = 100;
    }

    /**
     * @param name     The module unique identifier
     * @param desc     The module description
     * @param category The module category
     */
    public RotationModule(String name, String desc, ModuleCategory category, int rotationPriority)
    {
        super(name, desc, category);
        this.rotationPriority = rotationPriority;
    }

    protected void setRotation(float yaw, float pitch)
    {
        Managers.ROTATION.setRotation(new Rotation(getRotationPriority(), yaw, pitch));
    }

    protected void setRotationSilent(float yaw, float pitch)
    {
        Managers.ROTATION.setRotationSilent(yaw, pitch);
    }

    /**
     * Sets client look yaw and pitch
     *
     * @param yaw
     * @param pitch
     */
    protected void setRotationClient(float yaw, float pitch)
    {
        Managers.ROTATION.setRotationClient(yaw, pitch);
    }

    protected boolean isRotationBlocked()
    {
        return Managers.ROTATION.isRotationBlocked(getRotationPriority());
    }

    protected int getRotationPriority()
    {
        return rotationPriority;
    }
}

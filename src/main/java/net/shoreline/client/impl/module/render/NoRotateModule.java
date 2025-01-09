package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.ServerRotationEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class NoRotateModule extends ToggleModule
{
    Config<Boolean> positionAdjustConfig = register(new BooleanConfig("PositionAdjust", "Adjusts outgoing rotation packets", false));

    public NoRotateModule()
    {
        super("NoRotate", "Prevents server from forcing rotations", ModuleCategory.RENDER);
    }

    @EventListener
    public void onServerRotation(ServerRotationEvent event)
    {
        event.cancel();
        if (positionAdjustConfig.getValue())
        {
            float yaw = Managers.ROTATION.getServerYaw();
            float pitch = Managers.ROTATION.getServerPitch();
            if (Managers.ROTATION.isRotating())
            {
                yaw = Managers.ROTATION.getRotationYaw();
                pitch = Managers.ROTATION.getRotationPitch();
            }
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }
}

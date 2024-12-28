package net.shoreline.client.impl.module.render;

import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.MouseUpdateEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.camera.CameraRotationEvent;
import net.shoreline.client.impl.event.option.PerspectiveUpdateEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class FreeLookModule extends ToggleModule
{
    private float cameraYaw;
    private float cameraPitch;
    private Perspective perspective;

    public FreeLookModule()
    {
        super("FreeLook", "Allows you to freely move the camera in third person", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        perspective = mc.options.getPerspective();
    }

    @Override
    public void onDisable()
    {
        if (perspective != null)
        {
            mc.options.setPerspective(perspective);
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (perspective != null && perspective != Perspective.THIRD_PERSON_BACK)
        {
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    @EventListener
    public void onPerspectiveUpdate(PerspectiveUpdateEvent event)
    {
        if (mc.options.getPerspective() != event.getPerspective() && event.getPerspective() != Perspective.FIRST_PERSON)
        {
            cameraYaw = mc.player.getYaw();
            cameraPitch = mc.player.getPitch();
        }
    }

    @EventListener
    public void onCameraRotation(CameraRotationEvent event)
    {
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON)
        {
            event.setYaw(cameraYaw);
            event.setPitch(cameraPitch);
        }
    }

    @EventListener
    public void onMouseUpdate(MouseUpdateEvent event)
    {
        if (mc.options.getPerspective() != Perspective.FIRST_PERSON)
        {
            event.cancel();
            changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
        }
    }

    /**
     * @param cursorDeltaX
     * @param cursorDeltaY
     * @see net.minecraft.entity.Entity#changeLookDirection(double, double)
     */
    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY)
    {
        float f = (float) cursorDeltaY * 0.15F;
        float g = (float) cursorDeltaX * 0.15F;
        this.cameraPitch += f;
        this.cameraYaw += g;
        this.cameraPitch = MathHelper.clamp(cameraPitch, -90.0F, 90.0F);
    }
}

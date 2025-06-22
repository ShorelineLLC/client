package net.shoreline.client.impl.module.impl;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class MovementModule extends Toggleable
{
    public MovementModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public MovementModule(final String name,
                          final String[] nameAliases,
                          final String description,
                          final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public void onMove(final Vec3d movement)
    {

    }

    protected float getYawFromInput()
    {
        float yaw = mc.player.getYaw();
        boolean forward = mc.options.forwardKey.isPressed();
        boolean backward = mc.options.backKey.isPressed();
        boolean left = mc.options.leftKey.isPressed();
        boolean right = mc.options.rightKey.isPressed();
        if (forward && !backward)
        {
            if (left && !right)
            {
                yaw -= 45.0f;
            }
            else if (right && !left)
            {
                yaw += 45.0f;
            }
        }
        else if (backward && !forward)
        {
            yaw += 180.0f;
            if (left && !right)
            {
                yaw += 45.0f;
            }
            else if (right && !left)
            {
                yaw -= 45.0f;
            }
        }
        else if (left && !right)
        {
            yaw -= 90.0f;
        }
        else if (right && !left)
        {
            yaw += 90.0f;
        }
        return MathHelper.wrapDegrees(yaw);
    }

    protected boolean isInputtingMovement()
    {
        return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
    }
}

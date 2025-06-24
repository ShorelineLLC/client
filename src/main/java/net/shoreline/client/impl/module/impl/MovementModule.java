package net.shoreline.client.impl.module.impl;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
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

    protected Vec2f strafe(float speed)
    {
        float forward = mc.player.input.getMovementInput().y;
        float strafe = mc.player.input.getMovementInput().x;
        float yaw = mc.player.lastYaw + (mc.player.getYaw() - mc.player.lastYaw) * mc.getRenderTickCounter().getTickProgress(true);
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        }
        else if (forward != 0.0f)
        {
            if (strafe >= 1.0f)
            {
                yaw += forward > 0.0f ? -45 : 45;
                strafe = 0.0f;
            }
            else if (strafe <= -1.0f)
            {
                yaw += forward > 0.0f ? 45 : -45;
                strafe = 0.0f;
            }
            if (forward > 0.0f)
            {
                forward = 1.0f;
            }
            else if (forward < 0.0f)
            {
                forward = -1.0f;
            }
        }
        float rx = (float) Math.cos(Math.toRadians(yaw));
        float rz = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2f((forward * speed * rz) + (strafe * speed * rx),
                (forward * speed * rx) - (strafe * speed * rz));
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

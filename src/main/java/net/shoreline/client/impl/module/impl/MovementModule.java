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

    protected double getMotionX()
    {
        return mc.player.getVelocity().x;
    }

    protected double getMotionY()
    {
        return mc.player.getVelocity().y;
    }

    protected double getMotionZ()
    {
        return mc.player.getVelocity().z;
    }

    protected void setMotionY(double y)
    {
        mc.player.setVelocity(getMotionX(), y, getMotionZ());
    }

    protected void addMotionY(double y)
    {
        mc.player.setVelocity(mc.player.getVelocity().add(0.0, y, 0.0));
    }

    protected void setMotionXZ(double x, double z)
    {
        mc.player.setVelocity(x, getMotionY(), z);
    }

    protected Vec2f strafe(float speed)
    {
        float forward = mc.player.input.getMovementInput().y;
        float strafe = mc.player.input.getMovementInput().x;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getRenderTickCounter().getTickDelta(true);
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        }
        else if (forward != 0.0f)
        {
            if (strafe > 0.0)
            {
                yaw += forward > 0.0 ? -45 : 45;
            }
            else if (strafe < 0.0)
            {
                yaw += forward > 0.0 ? 45 : -45;
            }

            strafe = 0.0f;
            if (forward > 0.0)
            {
                forward = 1.0f;
            }
            else if (forward < 0.0)
            {
                forward = -1.0f;
            }
        }
        float cos = (float) Math.cos(Math.toRadians(yaw));
        float sin = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2f((forward * speed * sin) + (strafe * speed * cos),
                (forward * speed * cos) - (strafe * speed * sin));
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
}

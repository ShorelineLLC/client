package net.shoreline.client.impl.module.impl;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;

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

    public double getMotionX()
    {
        return mc.player.getVelocity().x;
    }

    public double getMotionY()
    {
        return mc.player.getVelocity().y;
    }

    public double getMotionZ()
    {
        return mc.player.getVelocity().z;
    }

    public void setMotionY(double y)
    {
        mc.player.setVelocity(getMotionX(), y, getMotionZ());
    }

    public void addMotionY(double y)
    {
        mc.player.setVelocity(mc.player.getVelocity().add(0.0, y, 0.0));
    }

    public void setMotionXZ(double x, double z)
    {
        mc.player.setVelocity(x, getMotionY(), z);
    }

    public Vec2f strafe(float speed)
    {
        float forward = mc.player.input.getMovementInput().y;
        float strafe = mc.player.input.getMovementInput().x;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        float yaw = Managers.ROTATION.hasClientRotation() ?
                Managers.ROTATION.getClientRotation().getYaw() :
                mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * tickDelta;

        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        } else if (forward != 0.0f)
        {
            if (strafe > 0.0)
            {
                yaw += forward > 0.0 ? -45 : 45;
            } else if (strafe < 0.0)
            {
                yaw += forward > 0.0 ? 45 : -45;
            }

            strafe = 0.0f;
            if (forward > 0.0)
            {
                forward = 1.0f;
            } else if (forward < 0.0)
            {
                forward = -1.0f;
            }
        }

        float cos = (float) Math.cos(Math.toRadians(yaw));
        float sin = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2f((forward * speed * sin) + (strafe * speed * cos),
                (forward * speed * cos) - (strafe * speed * sin));
    }

    protected double getAcceleratedSpeed(double baseSpeed,
                                         double maxSpeed,
                                         double accelMaxTime,
                                         long accelTime)
    {
        if (maxSpeed >= baseSpeed)
        {
            return baseSpeed;
        } else
        {
            double v8 = MathHelper.clamp((double) (System.currentTimeMillis() - accelTime) / (accelMaxTime * 1000.0), 0.0, 1.0);
            double v10 = maxSpeed + (baseSpeed - maxSpeed) * v8;
            return Math.min(v10, baseSpeed);
        }
    }
}

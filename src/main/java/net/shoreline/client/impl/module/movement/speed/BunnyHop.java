package net.shoreline.client.impl.module.movement.speed;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.movement.SpeedModule;
import net.shoreline.client.impl.module.world.TimerModule;

public class BunnyHop extends Strafe
{
    protected boolean accel;

    public BunnyHop()
    {
        super("BHop");
    }

    @Override
    public Vec3d onMoveUpdate(SpeedModule module, Vec3d currentMove)
    {
        double moveX = currentMove.x;
        double moveY = currentMove.y;
        double moveZ = currentMove.z;

        if (module.getUseTimerConfig().getValue())
        {
            TimerModule.INSTANCE.setTimerTicks(1.0888f);
        }

        double speedEffect = getSpeedModifier();
        double slowEffect = getSlownessModifier();
        double base = getBaseSpeed(speedEffect, slowEffect);
        if (strafe == 1)
        {
            speed = 1.35f * base - 0.01f;
        }

        else if (strafe == 2)
        {
            if (mc.player.input.playerInput.jump() || !mc.player.isOnGround())
            {
                return currentMove;
            }

            float jumpEffect = getJumpModifier();
            float jump = 0.3999999463558197f + jumpEffect;
            moveY = jump;
            module.setMotionY(moveY);
            speed *= accel ? 1.6835 : 1.395;
        }
        else if (strafe == 3)
        {
            double moveSpeed = 0.66 * (distance - base);
            speed = distance - moveSpeed;
            accel = !accel;
        }
        else
        {
            if ((!mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0,
                    mc.player.getVelocity().getY(), 0)) || mc.player.verticalCollision) && strafe > 0)
            {
                strafe = 1;
            }
            speed = distance - distance / AIR_FRICTION;
        }

        speed = Math.max(speed, base);
        final Vec2f motion = module.strafe((float) speed);
        moveX = motion.x;
        moveZ = motion.y;
        strafe++;
        return new Vec3d(moveX, moveY, moveZ);
    }

    @Override
    public void reset()
    {
        super.reset();
        accel = false;
//        strictTicks = 0;
    }
}

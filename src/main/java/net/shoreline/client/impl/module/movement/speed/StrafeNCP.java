package net.shoreline.client.impl.module.movement.speed;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.movement.SpeedModule;
import net.shoreline.client.impl.module.world.TimerModule;
import net.shoreline.client.util.math.MathUtil;

public class StrafeNCP extends Strafe
{
    protected static final SpeedModule SPEED_MODULE = SpeedModule.INSTANCE;
    private int strictTicks;

    public StrafeNCP()
    {
        super("StrafeStrict");
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

        if (module.getFastConfig().getValue() && MathUtil.round(mc.player.getY() - (int) mc.player.getY(), 3) == MathUtil.round(0.138, 3))
        {
            module.addMotionY(-0.08);
            moveY -= 0.09316090325960147;
            // mc.player.setPosition(mc.player.getX(), mc.player.getY() - 0.09316090325960147, mc.player.getZ());
        }

        double speedEffect = getSpeedModifier();
        double slowEffect = getSlownessModifier();
        double base = getBaseSpeed(speedEffect, slowEffect);
        if (strafe == 1)
        {
            speed = (module.getFastConfig().getValue() ? 1.38f : 1.35f) * base - 0.01f;
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
            module.setMotionY(jump);
            speed *= 2.149;
        }
        else if (strafe == 3)
        {
            double moveSpeed = 0.66 * (distance - base);
            speed = distance - moveSpeed;
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

        strictTicks++;
        speed = Math.max(speed, base);
        double baseMax = 0.465 * speedEffect / slowEffect;
        double baseMin = 0.44 * speedEffect / slowEffect;
        speed = Math.min(speed, strictTicks > 25 ? baseMax : baseMin);
        if (strictTicks > 50)
        {
            strictTicks = 0;
        }

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
        strictTicks = 0;
    }
}

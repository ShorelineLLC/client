package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.world.TimerModule;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.client.util.math.MathUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class LongJumpModule extends MovementModule
{
    Config<Float> boostSpeed = new NumberConfig.Builder<Float>("Boost")
            .setMin(0.1f).setMax(10.0f).setDefaultValue(4.0f)
            .setDescription("The jump boost speed").build();

    private int strafe = 4;
    private double speed;
    private double distance;

    private static final float AIR_FRICTION = 159.077f;

    public LongJumpModule()
    {
        super("LongJump", "Jump further", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (!checkNull())
        {
            double dx = mc.player.getX() - mc.player.prevX;
            double dz = mc.player.getZ() - mc.player.prevZ;
            distance = Math.sqrt(dx * dx + dz * dz);
        }
    }

    @EventListener(priority = -1001)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (checkNull() || event.getType() != MovementType.SELF)
        {
            return;
        }

        if (!canApplySpeed())
        {
            resetStrafe();
            return;
        }

        event.cancel();
        double moveX;
        double moveY = event.getMovement().y;
        double moveZ;

        double speedEffect = 1.0;
        double slowEffect = 1.0;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
        {
            double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            speedEffect = 1 + (0.2 * (amplifier + 1));
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
        {
            double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            slowEffect = 1 + (0.2 * (amplifier + 1));
        }

        final double base = 0.272f * speedEffect / slowEffect;

        if (strafe == 1)
        {
            speed = boostSpeed.getValue() * base - 0.01f;
        }
        else if (strafe == 2)
        {
            if (mc.player.input.playerInput.jump() || !mc.player.isOnGround())
            {
                return;
            }

            moveY = 0.40123128f;
            setMotionY(moveY);
            speed *= 2.149f;
        }
        else if (strafe == 3)
        {
            double moveSpeed = 0.76 * (distance - base);
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
        speed = Math.max(speed, base);
        final Vec2f motion = strafe((float) speed);
        moveX = motion.x;
        moveZ = motion.y;
        event.setMovement(new Vec3d(moveX, moveY, moveZ));
        strafe++;
    }

    private boolean canApplySpeed()
    {
        return Managers.ANTICHEAT.hasPassedSinceSetback(100)
                && InputUtil.isInputtingMovement()
                && !mc.player.getAbilities().flying
                && mc.player.getVehicle() == null
                && !mc.player.isGliding()
                && !mc.player.isHoldingOntoLadder()
                && mc.player.fallDistance <= 2.0f
                && !mc.player.isInLava()
                && !mc.player.isTouchingWater();
    }

    public void resetStrafe()
    {
        strafe = 4;
        speed = 0.0f;
        distance = 0.0;
    }
}

package net.shoreline.client.impl.module.movement.speed;

import lombok.Getter;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.movement.SpeedModule;
import net.shoreline.client.impl.module.world.TimerModule;

@Getter
public abstract class BaseSpeedFeature<M extends MovementModule>
        extends GenericFeature
{
    protected double speed;
    protected double distance;

    public BaseSpeedFeature(String name)
    {
        super(name);
    }

    public abstract Vec3d onMoveUpdate(M module, Vec3d currentMove);

    public void reset()
    {
        speed = 0.0;
        distance = 0.0;
        TimerModule.INSTANCE.setTimerTicks(1.0f);
    }

    public void onDistanceTraveled()
    {
        double dx = mc.player.getX() - mc.player.prevX;
        double dz = mc.player.getZ() - mc.player.prevZ;
        distance = Math.sqrt(dx * dx + dz * dz);
    }

    protected double getSpeedModifier()
    {
        double speedEffect = 1.0;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
        {
            double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            speedEffect = 1 + (0.2 * (amplifier + 1));
        }

        return speedEffect;
    }

    protected double getSlownessModifier()
    {
        double slowEffect = 1.0;
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
        {
            double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            slowEffect = 1 + (0.2 * (amplifier + 1));
        }

        return slowEffect;
    }

    protected double getBaseSpeed(double speedEffect, double slowEffect)
    {
        return 0.2873f * speedEffect / slowEffect;
    }

    protected double getBaseSpeed()
    {
        return getBaseSpeed(getSpeedModifier(), getSlownessModifier());
    }

    protected float getJumpModifier()
    {
        float jumpEffect = 0.0f;
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
        {
            jumpEffect += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
        }

        return jumpEffect;
    }
}

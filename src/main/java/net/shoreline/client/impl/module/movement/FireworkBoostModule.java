package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.mixin.accessor.AccessorFireworkRocketEntity;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author hockeyl8
 * @since 1.0
 */
public final class FireworkBoostModule extends ToggleModule
{
    Config<Double> speedConfig = register(new NumberConfig<>("Speed", "The initial speed of the rocket.", 1.0, 1.5, 10.0));
    Config<Boolean> accelerateConfig = register(new BooleanConfig("Accelerate", "Whether or not to accelerate the rocket.", false));
    Config<Double> accelSpeedConfig = register(new NumberConfig<>("AccelSpeed", "The speed to accelerate the rocket at.", 0.1, 0.5, 5.0, () -> accelerateConfig.getValue()));
    Config<Double> maxSpeedConfig = register(new NumberConfig<>("MaxSpeed", "The initial speed of the rocket.", 1.0, 3.0, 10.0, () -> accelerateConfig.getValue()));

    private int tickCounter;
    private double speed;

    public FireworkBoostModule()
    {
        super("FireworkBoost", "Allows you to change the acceleration of your firework.", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        speed = 0.0f;
    }

    @EventListener
    public void onTick(final TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE || mc.player.isInLava())
        {
            return;
        }
        if (isBoostedByRocket() && mc.player.isFallFlying())
        {
            final Vec3d vec3d = mc.player.getRotationVector();
            final Vec3d vec3d2 = mc.player.getVelocity();
            if (accelerateConfig.getValue())
            {
                speed += accelSpeedConfig.getValue() * tickCounter;
                if (speed < maxSpeedConfig.getValue())
                {
                    tickCounter++;
                }
                else
                {
                    speed = MathHelper.clamp(speed, 0, maxSpeedConfig.getValue());
                }
            }
            else
            {
                speed = speedConfig.getValue();
            }
            mc.player.setVelocity(vec3d2.add(
                    vec3d.x * 0.1 + (vec3d.x * speed - vec3d2.x) * 0.5,
                    vec3d.y * 0.1 + (vec3d.y * speed - vec3d2.y) * 0.5,
                    vec3d.z * 0.1 + (vec3d.z * speed - vec3d2.z) * 0.5));
        }
        else
        {
            speed = 0.0f;
        }
    }

    private boolean isBoostedByRocket()
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (entity instanceof FireworkRocketEntity rocket
                    && ((AccessorFireworkRocketEntity) rocket).hookWasShotByEntity()
                    && ((AccessorFireworkRocketEntity) rocket).hookGetShooter() == mc.player)
            {
                return true;
            }
        }
        return false;
    }
}

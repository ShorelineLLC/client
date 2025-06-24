package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.eventbus.annotation.EventListener;

public class SpeedModule extends MovementModule
{
    Config<SpeedMode> modeConfig = new EnumConfig.Builder<SpeedMode>("Mode")
            .setValues(SpeedMode.values())
            .setDescription("The mode for accelerating the player")
            .setDefaultValue(SpeedMode.VANILLA).build();

    private int strafe = 4;
    private double speed;
    private double distance;
    private boolean accel;

    private static final float AIR_FRICTION = 159.077f;

    public SpeedModule()
    {
        super("Speed", new String[] {"Strafe"}, "Move faster", GuiCategory.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        resetStrafe();
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (!checkNull())
        {
            double dx = mc.player.getX() - mc.player.lastX;
            double dz = mc.player.getZ() - mc.player.lastZ;
            distance = Math.sqrt(dx * dx + dz * dz);
        }
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (checkNull() || event.getType() != MovementType.SELF)
        {
            return;
        }

        if (!canStrafe())
        {
            resetStrafe();
            return;
        }

        event.cancel();
        double moveX = event.getMovement().x;
        double moveY = event.getMovement().y;
        double moveZ = event.getMovement().z;

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
        final double base = 0.2873f * speedEffect / slowEffect;
        float jumpEffect = 0.0f;
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
        {
            jumpEffect += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
        }
        switch (modeConfig.getValue())
        {
            case STRAFE ->
            {
                if (strafe == 1)
                {
                    speed = 1.35f * base - 0.01f;
                }
                else if (strafe == 2)
                {
                    if (mc.player.input.playerInput.jump() || !mc.player.isOnGround())
                    {
                        return;
                    }

                    float jump = 0.3999999463558197f + jumpEffect;
                    moveY = jump;
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
                        strafe = isInputtingMovement() ? 1 : 0;
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
        }
    }

    public void resetStrafe()
    {
        strafe = 4;
        speed = 0.0f;
        distance = 0.0;
        accel = false;
    }

    private boolean canStrafe()
    {
        return isInputtingMovement()
                || !mc.player.getAbilities().flying
                || !mc.player.isRiding()
                || !mc.player.isGliding()
                || !mc.player.isHoldingOntoLadder()
                || mc.player.fallDistance <= 2.0f;
    }

    public enum SpeedMode
    {
        VANILLA,
        STRAFE,
        STRAFE_STRICT
    }
}

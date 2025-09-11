package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.world.TimerModule;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.client.util.math.MathUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class SpeedModule extends MovementModule
{
    public static SpeedModule INSTANCE;

    Config<SpeedMode> modeConfig = new EnumConfig.Builder<SpeedMode>("Mode")
            .setValues(SpeedMode.values())
            .setDescription("The mode for accelerating the player")
            .setDefaultValue(SpeedMode.VANILLA).build();
    Config<Float> speedConfig = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.1f).setMax(5.0f).setDefaultValue(0.5f)
            .setVisible(() -> modeConfig.getValue() == SpeedMode.VANILLA)
            .setDescription("Movement speed").build();
    Config<Boolean> fastConfig = new BooleanConfig.Builder("Fast")
            .setDescription("Falls to the ground faster")
            .setVisible(() -> modeConfig.getValue() == SpeedMode.STRAFE_STRICT)
            .setDefaultValue(false).build();
    Config<Boolean> useTimerConfig = new BooleanConfig.Builder("UseTimer")
            .setDescription("Uses timer to move faster")
            .setVisible(() -> modeConfig.getValue() == SpeedMode.STRAFE || modeConfig.getValue() == SpeedMode.STRAFE_STRICT)
            .setDefaultValue(false).build();
    Config<Boolean> inWaterConfig = new BooleanConfig.Builder("InWater")
            .setDescription("Applies speed when in water/lava")
            .setDefaultValue(false).build();

    private int strafe = 4;
    private double speed;
    private double distance;
    private boolean accel;

    private int strictTicks;

    private static final float AIR_FRICTION = 159.077f;

    public SpeedModule()
    {
        super("Speed", new String[] {"Strafe"}, "Move faster", GuiCategory.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return Formatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable()
    {
        resetStrafe();
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
                if (useTimerConfig.getValue())
                {
                    TimerModule.INSTANCE.setTimerTicks(1.0888f);
                }

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
                    setMotionY(moveY);
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
                final Vec2f motion = strafe((float) speed);
                moveX = motion.x;
                moveZ = motion.y;
                event.setMovement(new Vec3d(moveX, moveY, moveZ));
                strafe++;
            }
            case STRAFE_STRICT ->
            {
                if (useTimerConfig.getValue())
                {
                    TimerModule.INSTANCE.setTimerTicks(1.0888f);
                }

                if (fastConfig.getValue() && MathUtil.round(mc.player.getY() - (int) mc.player.getY(), 3) == MathUtil.round(0.138, 3))
                {
                    addMotionY(-0.08);
                    moveY -= 0.09316090325960147;
                    // mc.player.setPosition(mc.player.getX(), mc.player.getY() - 0.09316090325960147, mc.player.getZ());
                }

                if (strafe == 1)
                {
                    speed = (fastConfig.getValue() ? 1.38f : 1.35f) * base - 0.01f;
                }
                else if (strafe == 2)
                {
                    if (mc.player.input.playerInput.jump() || !mc.player.isOnGround())
                    {
                        return;
                    }
                    float jump = 0.3999999463558197f + jumpEffect;
                    moveY = jump;
                    setMotionY(jump);
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
                final Vec2f motion = strafe((float) speed);
                moveX = motion.x;
                moveZ = motion.y;
                event.setMovement(new Vec3d(moveX, moveY, moveZ));
                strafe++;
            }
            case VANILLA ->
            {
                Vec2f motion = strafe(speedConfig.getValue());
                moveX = motion.x;
                moveZ = motion.y;
                event.setMovement(new Vec3d(moveX, moveY, moveZ));
            }
        }
    }

    public void resetStrafe()
    {
        strafe = 4;
        speed = 0.0f;
        distance = 0.0;
        accel = false;
        strictTicks = 0;
        TimerModule.INSTANCE.setTimerTicks(1.0f);
    }

    private boolean canApplySpeed()
    {
        return Managers.ANTICHEAT.hasPassedSinceSetback(100)
                && InputUtil.isInputtingMovement()
                && !mc.player.getAbilities().flying
                && !mc.player.isRiding()
                && !mc.player.isGliding()
                && !mc.player.isHoldingOntoLadder()
                && mc.player.fallDistance <= 2.0f
                && ((!mc.player.isInLava() && !mc.player.isTouchingWater()) || inWaterConfig.getValue());
    }

    public enum SpeedMode
    {
        VANILLA,
        STRAFE,
        STRAFE_STRICT
    }
}

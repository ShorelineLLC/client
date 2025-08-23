package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.eventbus.annotation.EventListener;

public class AntiAimModule extends Toggleable
{
    Config<YawMode> yawConfig = new EnumConfig.Builder<YawMode>("Yaw")
            .setValues(YawMode.values())
            .setDescription("The rotation spin mode for yaw")
            .setDefaultValue(YawMode.SPIN).build();
    Config<PitchMode> pitchConfig = new EnumConfig.Builder<PitchMode>("Pitch")
            .setValues(PitchMode.values())
            .setDescription("The rotation spin mode for pitch")
            .setDefaultValue(PitchMode.NONE).build();
    Config<Float> spinSpeedConfig = new NumberConfig.Builder<Float>("SpinSpeed")
            .setMin(1.0f).setMax(50.0f).setDefaultValue(15.0f)
            .setDescription("The rotation spin speed")
            .setVisible(() -> yawConfig.getValue() == YawMode.SPIN).build();
    Config<Integer> yawAngleConfig = new NumberConfig.Builder<Integer>("YawAngle")
            .setMin(-180).setMax(180).setDefaultValue(0).setFormat("deg")
            .setDescription("The rotation spin yaw angle")
            .setVisible(() -> yawConfig.getValue() == YawMode.STATIC || yawConfig.getValue() == YawMode.JITTER).build();
    Config<Integer> pitchAngleConfig = new NumberConfig.Builder<Integer>("PitchAngle")
            .setMin(-90).setMax(90).setDefaultValue(0).setFormat("deg")
            .setDescription("The rotation spin pitch angle")
            .setVisible(() -> pitchConfig.getValue() == PitchMode.STATIC || pitchConfig.getValue() == PitchMode.JITTER).build();
    Config<Integer> jitterTicksConfig = new NumberConfig.Builder<Integer>("JitterTicks")
            .setMin(1).setMax(20).setDefaultValue(2)
            .setDescription("The ticks between each random rotation")
            .setVisible(() -> yawConfig.getValue() == YawMode.JITTER).build();

    private Rotation current;
    private Rotation playerRotation;

    public AntiAimModule()
    {
        super("AntiAim", new String[] {"SpinBot"}, "Makes it harder for enemies to hit headshots", GuiCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull())
        {
            playerRotation = new Rotation(mc.player);
            current = playerRotation;
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onRotation(ClientRotationEvent event)
    {
        if (event.isCanceled())
        {
            return;
        }

        if (mc.options.attackKey.isPressed() || mc.options.useKey.isPressed())
        {
            return;
        }

        current = new Rotation(getYaw(), getPitch());
        event.cancel();
        event.setRotation(current);
    }

    public float getYaw()
    {
        return switch (yawConfig.getValue())
        {
            case NONE -> mc.player.getYaw();
            case STATIC -> mc.player.getYaw() + yawAngleConfig.getValue();
            case ZERO -> playerRotation == null ? 0.0f : playerRotation.getYaw();
            case SPIN ->
            {
                if (current == null)
                {
                    yield 0.0f;
                }
                float spin = current.getYaw() + spinSpeedConfig.getValue();
                if (spin > 360.0f)
                {
                    yield spin - 360.0f;
                }
                yield spin;
            }
            case JITTER -> mc.player.getYaw() + ((mc.player.age % jitterTicksConfig.getValue() == 0) ?
                    yawAngleConfig.getValue() : -yawAngleConfig.getValue());
        };
    }

    public float getPitch()
    {
        return switch (pitchConfig.getValue())
        {
            case NONE -> mc.player.getPitch();
            case STATIC -> pitchAngleConfig.getValue();
            case ZERO -> playerRotation == null ? 0.0f : playerRotation.getPitch();
            case UP -> -90.0f;
            case DOWN -> 90.0f;
            case JITTER ->
            {
                if (current == null)
                {
                    yield 0.0f;
                }
                float jitter = current.getPitch() + 30.0f;
                if (jitter > 90.0f)
                {
                    yield -90.0f;
                }
                if (jitter < -90.0f)
                {
                    yield 90.0f;
                }
                yield jitter;
            }
        };
    }

    public enum YawMode
    {
        NONE, STATIC, ZERO, SPIN, JITTER
    }

    public enum PitchMode
    {
        NONE, STATIC, ZERO, UP, DOWN, JITTER
    }
}

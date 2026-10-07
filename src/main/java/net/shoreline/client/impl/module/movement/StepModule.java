package net.shoreline.client.impl.module.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.event.entity.StepHeightEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.world.TimerModule;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

public class StepModule extends ListeningToggleable
{
    Config<Float> heightConfig = new NumberConfig.Builder<Float>("Height")
            .setMin(1.0f).setMax(2.5f).setDefaultValue(2.0f)
            .setDescription("The maximum step height").build();
    Config<StepMode> stepMode = new EnumConfig.Builder<StepMode>("Mode")
            .setValues(StepMode.values())
            .setDescription("The spoofing mode when attempting to step")
            .setDefaultValue(StepMode.VANILLA).build();
    Config<Boolean> timerConfig = new BooleanConfig.Builder("UseTimer")
            .setDescription("Uses timer to prevent packet spam")
            .setVisible(() -> stepMode.getValue() == StepMode.NCP)
            .setDefaultValue(false).build();

    private final Timer stepTimer = new NanoTimer();
    private boolean cancelTimer;

    public StepModule()
    {
        super("Step", "Step up blocks", GuiCategory.MOVEMENT);
    }

    @Override
    public String getModuleData()
    {
        return Formatter.formatEnum(stepMode.getValue());
    }

    @Override
    public void onDisable()
    {
        TimerModule.INSTANCE.setTimerTicks(1.0f);
        cancelTimer = false;
    }

    @EventListener
    public void onStepHeight(StepHeightEvent event)
    {
        event.cancel();

        if (checkNull() || !mc.player.isOnGround())
        {
            event.setStepHeight(0.6f);
            return;
        }

        if (stepTimer.hasPassed(200))
        {
            event.setStepHeight(heightConfig.getValue());
        }
    }

    @EventListener
    public void onPlayerUpdatePre(PlayerUpdateEvent.Pre event)
    {
        if (cancelTimer)
        {
            TimerModule.INSTANCE.setTimerTicks(1.0f);
            cancelTimer = false;
        }
    }

    @EventListener
    public void onPlayerUpdatePeri(PlayerUpdateEvent.Peri event)
    {
        if (checkNull())
        {
            return;
        }

        if (stepMode.getValue() == StepMode.NCP || stepMode.getValue() == StepMode.STRICT_NCP)
        {
            double stepHeight = mc.player.getY() - mc.player.prevY;
            if (stepHeight <= 0.5 || stepHeight > heightConfig.getValue())
            {
                return;
            }

            double[] offs = getStepOffsets(stepHeight);
            if (offs == null)
            {
                return;
            }

            if (timerConfig.getValue())
            {

                TimerModule.INSTANCE.setTimerTicks(stepHeight > 1.0 ? 0.15f : 0.35f);
                cancelTimer = true;
            }

            for (double off : offs)
            {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.prevX,
                        mc.player.prevY + off,
                        mc.player.prevZ,
                        false,
                        mc.player.horizontalCollision));
            }

            stepTimer.reset();
        }
    }

    // Credit: doogie
    private double[] getStepOffsets(double stepHeight)
    {
        double[] offsets = null;
        if (stepMode.getValue() == StepMode.STRICT_NCP)
        {
            if (stepHeight > 1.1661)
            {
                offsets = new double[] {0.42, 0.7532, 1.001, 1.1661, stepHeight};
            } else if (stepHeight > 1.015)
            {
                offsets = new double[] {0.42, 0.7532, 1.001, stepHeight};
            } else if (stepHeight > 0.6)
            {
                offsets = new double[] {0.42 * stepHeight, 0.7532 * stepHeight, stepHeight};
            }

            return offsets;
        }

        if (stepHeight > 2.019)
        {
            offsets = new double[] {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919};
        } else if (stepHeight > 1.5)
        {
            offsets = new double[] {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        } else if (stepHeight > 1.015)
        {
            offsets = new double[] {0.42, 0.7532, 1.01, 1.093, 1.015};
        } else if (stepHeight > 0.6)
        {
            offsets = new double[] {0.42 * stepHeight, 0.7532 * stepHeight};
        }

        return offsets;
    }

    private enum StepMode
    {
        NCP, STRICT_NCP, VANILLA
    }
}

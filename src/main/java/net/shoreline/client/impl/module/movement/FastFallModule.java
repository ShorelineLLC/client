package net.shoreline.client.impl.module.movement;

import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.event.network.TickMovementEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.eventbus.annotation.EventListener;

public class FastFallModule extends MovementModule
{
    Config<FallMode> modeConfig = new EnumConfig.Builder<FallMode>("Mode")
            .setValues(FallMode.values())
            .setDescription("The mode for increasing fall speed")
            .setDefaultValue(FallMode.STEP).build();
    Config<Float> heightConfig = new NumberConfig.Builder<Float>("Height")
            .setMin(1.0f).setMax(10.0f).setDefaultValue(3.0f)
            .setDescription("The fall height to activate fast fall").build();
    Config<Integer> shiftTicksConfig = new NumberConfig.Builder<Integer>("Ticks")
            .setMin(1).setMax(5).setDefaultValue(1)
            .setVisible(() -> modeConfig.getValue() == FallMode.SHIFT)
            .setDescription("The number of ticks to shift").build();

    private boolean prevOnGround;
    private boolean cancelFallMovement;
    private int fallTicks;
    private final Timer fallTimer = new NanoTimer();

    public FastFallModule()
    {
        super("FastFall", new String[] {"ReverseStep"}, "Falls faster", GuiCategory.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        cancelFallMovement = false;
        fallTicks = 0;
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        prevOnGround = mc.player.isOnGround();
        if (modeConfig.getValue() == FallMode.STEP)
        {
            if (!Managers.ANTICHEAT.hasPassedSinceSetback(1000)
                    || SpeedModule.INSTANCE.isEnabled()
                    || FlightModule.INSTANCE.isEnabled()
                    || !canFastFall())
            {
                return;
            }

            double fallHeight = traceDown();
            if (fallHeight > 0.01 && fallHeight <= heightConfig.getValue() && mc.player.isOnGround())
            {
                mc.player.setVelocity(getMotionX() * 0.05, -3.0, getMotionZ() * 0.05);
                // Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            }
        }
    }

    @EventListener
    public void onTickMovement(TickMovementEvent event)
    {
        if (modeConfig.getValue() == FallMode.SHIFT)
        {
            if (!Managers.ANTICHEAT.hasPassedSinceSetback(1000)
                    || SpeedModule.INSTANCE.isEnabled()
                    || FlightModule.INSTANCE.isEnabled()
                    || !canFastFall()
                    || !fallTimer.hasPassed(1000))
            {
                return;
            }

            double fallHeight = traceDown();
            if (fallHeight > 0.01 && fallHeight <= heightConfig.getValue() + 0.01)
            {
                if (mc.player.isOnGround())
                {
                    setMotionXZ(getMotionX() * 0.05, getMotionZ() * 0.05);
                }

                if (getMotionY() < 0 && prevOnGround && !mc.player.isOnGround())
                {
                    fallTimer.reset();
                    event.cancel();
                    event.setIterations(shiftTicksConfig.getValue());
                    cancelFallMovement = true;
                    fallTicks = 0;
                }
            }
        }
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (cancelFallMovement && modeConfig.getValue() == FallMode.SHIFT)
        {
            event.setMovement(event.getMovement().multiply(0.0, 1.0, 0.0));
            setMotionXZ(0.0, 0.0);
            ++fallTicks;
            if (fallTicks > shiftTicksConfig.getValue())
            {
                cancelFallMovement = false;
                fallTicks = 0;
            }
        }
    }

    private double traceDown()
    {
        Box bb = mc.player.getBoundingBox();
        for (double i = 0.0; i < heightConfig.getValue() + 0.5; i += 0.01)
        {
            if (!mc.world.isSpaceEmpty(mc.player, bb.offset(0.0, -i, 0.0)))
            {
                return i;
            }
        }
        return -1.0;
    }

    private boolean canFastFall()
    {
        return mc.player.getVehicle() == null
                && !mc.player.isGliding()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.isInLava()
                && !mc.player.isTouchingWater()
                && !mc.player.input.playerInput.jump()
                && !mc.player.input.playerInput.sneak();
    }

    public enum FallMode
    {
        STEP,
        SHIFT
    }
}

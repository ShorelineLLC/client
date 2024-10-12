package net.shoreline.client.impl.module.movement;

import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.player.PlayerMoveEvent;
import net.shoreline.client.impl.event.network.TickMovementEvent;
import net.shoreline.client.impl.module.exploit.PacketFlyModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class FastFallModule extends ToggleModule
{
    //
    Config<Float> heightConfig = register(new NumberConfig<>("Height", "The maximum fall height", 1.0f, 3.0f, 10.0f));
    Config<FallMode> fallModeConfig = register(new EnumConfig<>("Mode", "The mode for falling down blocks", FallMode.STEP, FallMode.values()));
    Config<Boolean> accelerateConfig = register(new BooleanConfig("Accelerate", "Accels the fall speed", false, () -> fallModeConfig.getValue() == FallMode.STEP));
    Config<Integer> shiftTicksConfig = register(new NumberConfig<>("ShiftTicks", "Number of ticks to shift ahead", 1, 3, 5, () -> fallModeConfig.getValue() == FallMode.SHIFT));
    //
    private boolean prevOnGround;
    //
    private boolean cancelFallMovement;
    private int fallTicks;
    private final Timer fallTimer = new CacheTimer();

    /**
     *
     */
    public FastFallModule()
    {
        super("FastFall", "Falls down blocks faster", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        cancelFallMovement = false;
        fallTicks = 0;
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE)
        {
            prevOnGround = mc.player.isOnGround();
            if (fallModeConfig.getValue() == FallMode.STEP)
            {
                if (mc.player.isRiding()
                        || mc.player.isFallFlying()
                        || mc.player.isHoldingOntoLadder()
                        || mc.player.isInLava()
                        || mc.player.isTouchingWater()
                        || mc.player.input.jumping
                        || mc.player.input.sneaking
                        || !Managers.ANTICHEAT.hasPassed(1000))
                {
                    return;
                }
                if (SpeedModule.getInstance().isEnabled() || LongJumpModule.getInstance().isEnabled()
                        || FlightModule.getInstance().isEnabled() || PacketFlyModule.getInstance().isEnabled())
                {
                    return;
                }
                double fallHeight = traceDown();
                if (fallHeight > 0.01 && fallHeight <= heightConfig.getValue() && mc.player.isOnGround())
                {
                    Managers.MOVEMENT.setMotionXZ(mc.player.getVelocity().x * 0.05, mc.player.getVelocity().z * 0.05);
                    Managers.MOVEMENT.setMotionY(accelerateConfig.getValue() ? mc.player.getVelocity().y - 0.62f : -3.0);
                    // Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
                }
            }
        }
    }

    @EventListener
    public void onTickMovement(TickMovementEvent event)
    {
        if (fallModeConfig.getValue() == FallMode.SHIFT)
        {
            if (mc.player.isRiding()
                    || mc.player.isFallFlying()
                    || mc.player.isHoldingOntoLadder()
                    || mc.player.isInLava()
                    || mc.player.isTouchingWater()
                    || mc.player.input.jumping
                    || mc.player.input.sneaking
                    || !Managers.ANTICHEAT.hasPassed(1000))
            {
                return;
            }
            if (!Managers.ANTICHEAT.hasPassed(1000) || !fallTimer.passed(1000)
                    || SpeedModule.getInstance().isEnabled() || LongJumpModule.getInstance().isEnabled()
                    || FlightModule.getInstance().isEnabled() || PacketFlyModule.getInstance().isEnabled())
            {
                return;
            }
            double fallHeight = traceDown();
            if (fallHeight > 0.01 && fallHeight <= heightConfig.getValue() + 0.01)
            {
                if (mc.player.isOnGround())
                {
                    Managers.MOVEMENT.setMotionXZ(mc.player.getVelocity().x * 0.05, mc.player.getVelocity().z * 0.05);
                }
                if (mc.player.getVelocity().y < 0 && prevOnGround && !mc.player.isOnGround())
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
        if (FlightModule.getInstance().isEnabled() || PacketFlyModule.getInstance().isEnabled())
        {
            return;
        }
        if (cancelFallMovement && fallModeConfig.getValue() == FallMode.SHIFT)
        {
            event.setX(0.0);
            event.setZ(0.0);
            Managers.MOVEMENT.setMotionXZ(0.0, 0.0);
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

    public enum FallMode
    {
        STEP,
        SHIFT
    }
}

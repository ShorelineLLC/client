package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.module.exploit.FastLatencyModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;

/**
 * @author linus & hockeyl8
 * @since 1.0
 */
public class FlightModule extends ToggleModule {
    private static FlightModule INSTANCE;

    Config<FlightMode> modeConfig = new EnumConfig<>("Mode", "The mode for vanilla flight", FlightMode.NORMAL, FlightMode.values());
    Config<Float> speedConfig = new NumberConfig<>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f);
    Config<Float> vspeedConfig = new NumberConfig<>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f);
    Config<Boolean> antiKickConfig = new BooleanConfig("AntiKick", "Prevents vanilla flight detection", true);
    Config<Boolean> accelerateConfig = new BooleanConfig("Accelerate", "Accelerate as you fly", false);
    Config<Float> accelerateSpeedConfig = new NumberConfig<>("AccelerateSpeed", "Speed to accelerate as", 0.01f, 0.2f, 1.0f, () -> accelerateConfig.getValue());
    Config<Float> maxSpeedConfig = new NumberConfig<>("MaxSpeed", "Max speed to acceleratee to", 1.0f, 5.0f, 10.0f, () -> accelerateConfig.getValue());

    private double speed;
    private final Timer antiKickTimer = new CacheTimer();
    private final Timer antiKick2Timer = new CacheTimer();

    public FlightModule() {
        super("Flight", "Allows the player to fly in survival", ModuleCategory.MOVEMENT);
        INSTANCE = this;
    }

    public static FlightModule getInstance() {
        return INSTANCE;
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onEnable() {
        antiKickTimer.reset();
        antiKick2Timer.reset();
        if (modeConfig.getValue() == FlightMode.VANILLA) {
            enableVanillaFly();
        }
        speed = 0.0;
    }

    @Override
    public void onDisable() {
        if (modeConfig.getValue() == FlightMode.VANILLA) {
            disableVanillaFly();
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        if (accelerateConfig.getValue()) {
            if (!MovementUtil.isInputtingMovement() || mc.player.horizontalCollision) {
                speed = 0.0f;
            }
            speed += accelerateSpeedConfig.getValue();
            if (speed > maxSpeedConfig.getValue()) {
                speed = maxSpeedConfig.getValue();
            }
        }
        else {
            speed = speedConfig.getValue();
        }
        if (modeConfig.getValue().equals(FlightMode.VANILLA)) {
            mc.player.getAbilities().setFlySpeed((float) (speed * 0.05f));
        }
        else {
            mc.player.getAbilities().setFlySpeed(0.05f);
        }
        // Vanilla fly kick checks every 80 ticks
        if (antiKickTimer.passed(3900) && antiKickConfig.getValue()) {
            Managers.MOVEMENT.setMotionY(-0.04);
            antiKickTimer.reset();
        } else if (antiKick2Timer.passed(4000) && antiKickConfig.getValue()) {
            Managers.MOVEMENT.setMotionY(0.04);
            antiKick2Timer.reset();
        } else if (modeConfig.getValue() == FlightMode.NORMAL) {
            Managers.MOVEMENT.setMotionY(0.0);
            if (mc.options.jumpKey.isPressed()) {
                Managers.MOVEMENT.setMotionY(vspeedConfig.getValue());
            } else if (mc.options.sneakKey.isPressed()) {
                Managers.MOVEMENT.setMotionY(-vspeedConfig.getValue());
            }
        }
        if (modeConfig.getValue() == FlightMode.NORMAL) {
            speed = Math.max(speed, 0.2873f);
            float forward = mc.player.input.movementForward;
            float strafe = mc.player.input.movementSideways;
            float yaw = mc.player.getYaw();
            if (forward == 0.0f && strafe == 0.0f) {
                Managers.MOVEMENT.setMotionXZ(0.0f, 0.0f);
                return;
            }
            double rx = Math.cos(Math.toRadians(yaw + 90.0f));
            double rz = Math.sin(Math.toRadians(yaw + 90.0f));
            Managers.MOVEMENT.setMotionXZ((forward * speed * rx) + (strafe * speed * rz),
                    (forward * speed * rz) - (strafe * speed * rx));
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (event.getConfig() == modeConfig && event.getStage() == EventStage.POST) {
            if (modeConfig.getValue() == FlightMode.VANILLA) {
                enableVanillaFly();
            } else {
                disableVanillaFly();
            }
        }
    }

    private void enableVanillaFly() {
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
    }

    private void disableVanillaFly() {
        if (!mc.player.isCreative()) {
            mc.player.getAbilities().allowFlying = false;
        }
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05f);
    }

    public enum FlightMode {
        NORMAL,
        VANILLA
    }
}

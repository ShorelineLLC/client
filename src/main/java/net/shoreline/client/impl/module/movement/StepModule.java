package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.string.EnumFormatter;

/**
 * @author linus
 * @since 1.0
 */
public class StepModule extends ToggleModule {

    //
    Config<StepMode> modeConfig = new EnumConfig<>("Mode", "Step mode", StepMode.NORMAL, StepMode.values());
    Config<Float> heightConfig = new NumberConfig<>("Height", "The maximum height for stepping up blocks", 1.0f, 2.5f, 10.0f);
    Config<Boolean> useTimerConfig = new BooleanConfig("UseTimer", "Slows down packets by applying timer when stepping", true, () -> modeConfig.getValue() == StepMode.NORMAL);
    Config<Boolean> strictConfig = new BooleanConfig("Strict", "Confirms the step height for NCP servers", false, () -> heightConfig.getValue() <= 2.5f);
    Config<Boolean> entityStepConfig = new BooleanConfig("EntityStep", "Allows entities to step up blocks", false);
    private boolean cancelTimer;
    //
    private final Timer stepTimer = new CacheTimer();

    /**
     *
     */
    public StepModule() {
        super("Step", "Allows the player to step up blocks",
                ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        setStepHeight(isAbstractHorse(mc.player.getVehicle()) ? 1.0f : 0.6f);
        Managers.TICK.setClientTick(1.0f);
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (modeConfig.getValue() == StepMode.NORMAL) {
            double stepHeight = mc.player.getY() - mc.player.prevY;
            if (stepHeight <= 0.5 || stepHeight > heightConfig.getValue()) {
                return;
            }
            //
            final double[] offs = getStepOffsets(stepHeight);
            if (useTimerConfig.getValue()) {
                Managers.TICK.setClientTick(stepHeight > 1.0 ? 0.15f : 0.35f);
                cancelTimer = true;
            }
            for (double off : offs) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.prevX,
                        mc.player.prevY + off, mc.player.prevZ, false));
            }
            stepTimer.reset();
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() != EventStage.PRE) {
            return;
        }
        if (mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.isFallFlying()) {
            Managers.TICK.setClientTick(1.0f);
            setStepHeight(isAbstractHorse(mc.player.getVehicle()) ? 1.0f : 0.6f);
            return;
        }
        if (cancelTimer && mc.player.isOnGround()) {
            Managers.TICK.setClientTick(1.0f);
            cancelTimer = false;
        }
        if (mc.player.isOnGround() && stepTimer.passed(200)) {
            setStepHeight(heightConfig.getValue());
        } else {
            setStepHeight(isAbstractHorse(mc.player.getVehicle()) ? 1.0f : 0.6f);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            disable();
        }
    }

    private void setStepHeight(float stepHeight) {
        if (entityStepConfig.getValue() && mc.player.getVehicle() != null) {
            mc.player.getVehicle().setStepHeight(stepHeight);
        } else {
            mc.player.setStepHeight(stepHeight);
        }
    }

    /**
     * @param stepHeight The step height
     * @return
     */
    private double[] getStepOffsets(double stepHeight) {
        // Credit: doogie
        double[] offsets = new double[0];
        if (strictConfig.getValue()) {
            if (stepHeight > 1.1661) {
                offsets = new double[] {0.42, 0.7532, 1.001, 1.1661, stepHeight};
            } else if (stepHeight > 1.015) {
                offsets = new double[] {0.42, 0.7532, 1.001, stepHeight};
            } else if (stepHeight > 0.6) {
                offsets = new double[] {0.42 * stepHeight, 0.7532 * stepHeight, stepHeight};
            }
            return offsets;
        }
        if (stepHeight > 2.019) {
            offsets = new double[] {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919};
        } else if (stepHeight > 1.5) {
            offsets = new double[] {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        } else if (stepHeight > 1.015) {
            offsets = new double[] {0.42, 0.7532, 1.01, 1.093, 1.015};
        } else if (stepHeight > 0.6) {
            offsets = new double[] {0.42 * stepHeight, 0.7532 * stepHeight};
        }
        return offsets;
    }

    private boolean isAbstractHorse(Entity e) {
        return e instanceof HorseEntity || e instanceof LlamaEntity || e instanceof MuleEntity;
    }

    public enum StepMode {
        VANILLA,
        NORMAL,
        A_A_C
    }
}

package net.shoreline.client.impl.module.movement;

import lombok.Getter;
import net.minecraft.entity.MovementType;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.movement.speed.BaseSpeedFeature;
import net.shoreline.client.impl.module.movement.speed.SpeedMode;
import net.shoreline.client.impl.module.movement.speed.Strafe;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
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
            .setVisible(() -> modeConfig.getValue().getFeature() instanceof Strafe)
            .setDefaultValue(false).build();
    Config<Boolean> inWaterConfig = new BooleanConfig.Builder("InWater")
            .setDescription("Applies speed when in water/lava")
            .setDefaultValue(false).build();

    public SpeedModule()
    {
        super("Speed", new String[] {"Strafe"}, "Move faster", GuiCategory.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return modeConfig.getValue().getFeature().getName();
    }

    @Override
    public void onDisable()
    {
        modeConfig.getValue().getFeature().reset();
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (!checkNull())
        {
            modeConfig.getValue().getFeature().onDistanceTraveled();
        }
    }

    @EventListener(priority = -1001)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (checkNull() || event.getType() != MovementType.SELF)
        {
            return;
        }

        BaseSpeedFeature<SpeedModule> speedFeature = modeConfig.getValue().getFeature();
        if (!canApplySpeed())
        {
            speedFeature.reset();
            return;
        }

        event.cancel();
        event.setMovement(speedFeature.onMoveUpdate(this, event.getMovement()));
    }

    private boolean canApplySpeed()
    {
        return Managers.ANTICHEAT.hasPassedSinceSetback(100)
                && InputUtil.isInputtingMovement()
                && !mc.player.getAbilities().flying
                && mc.player.getVehicle() == null
                && !mc.player.isGliding()
                && !mc.player.isHoldingOntoLadder()
                && mc.player.fallDistance <= 2.0f
                && ((!mc.player.isInLava() && !mc.player.isTouchingWater()) || inWaterConfig.getValue());
    }
}

package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.PlayerJumpEvent;
import net.shoreline.client.impl.event.network.StopSprintingEvent;
import net.shoreline.client.impl.module.combat.util.PhaseUtil;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.render.FreecamModule;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class SprintModule extends MovementModule
{
    Config<SprintMode> modeConfig = new EnumConfig.Builder<SprintMode>("Mode")
            .setValues(SprintMode.values())
            .setDescription("The Sprinting mode. Rage allows for multi-directional sprinting")
            .setDefaultValue(SprintMode.LEGIT).build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates before sprinting horizontally/backwards")
            .setVisible(() -> modeConfig.getValue().equals(SprintMode.RAGE))
            .setDefaultValue(false).build();
    Config<Boolean> jumpFixConfig = new BooleanConfig.Builder("JumpFix")
            .setDescription("Fixes jumping slowdown in Rage sprint")
            .setVisible(() -> modeConfig.getValue().equals(SprintMode.RAGE))
            .setDefaultValue(false).build();

    public SprintModule()
    {
        super("Sprint", "Automatically sprints", GuiCategory.MOVEMENT);
    }

    @Override
    public String getModuleData()
    {
        return Formatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onTickPre(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (!canSprint() || !checkCollisions())
        {
            mc.player.setSprinting(false);
            return;
        }

        switch (modeConfig.getValue())
        {
            case LEGIT ->
            {
                if (mc.player.input.hasForwardMovement())
                {
                    mc.player.setSprinting(true);
                }
            }

            case RAGE ->
            {
                float sprintYaw = InputUtil.getYawFromInput(mc.player.getYaw());
                if (rotateConfig.getValue() && !Managers.ROTATION.isFacingYaw(sprintYaw))
                {
                    mc.player.setSprinting(false);
                    return;
                }

                mc.player.setSprinting(true);
            }
        }
    }

    @EventListener
    public void onClientRotation(ClientRotationEvent event)
    {
        if (modeConfig.getValue() != SprintMode.RAGE || !rotateConfig.getValue() || !canSprint())
        {
            return;
        }

        if (event.isCanceled() || FreecamModule.INSTANCE.isEnabled())
        {
            return;
        }

        float sprintYaw = InputUtil.getYawFromInput(mc.player.getYaw());
        event.cancel();
        event.setYaw(sprintYaw);
    }

    @EventListener
    public void onStopSprinting(StopSprintingEvent event)
    {
        if (canSprint() && checkCollisions() && modeConfig.getValue() == SprintMode.RAGE)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onJumpYaw(PlayerJumpEvent.Yaw event)
    {
        if (jumpFixConfig.getValue() && modeConfig.getValue() == SprintMode.RAGE)
        {
            float yaw = event.getYaw();
            float forward = Math.signum(mc.player.input.getMovementInput().y);
            float strafe = 90.0f * Math.signum(mc.player.input.getMovementInput().x);
            if (forward != 0.0f)
            {
                strafe *= (forward * 0.5f);
            }
            yaw -= strafe;
            if (forward < 0.0f)
            {
                yaw -= 180.0f;
            }

            event.receiveCanceled();
            event.setYaw(yaw);
        }
    }

    private boolean canSprint()
    {
        return InputUtil.isInputtingMovement()
                && !PhaseUtil.isInsideWeb(mc.player)
                && !mc.player.isSneaking()
                && mc.player.getVehicle() == null
                && !mc.player.isGliding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0f;
    }

    private boolean checkCollisions()
    {
        return !mc.player.horizontalCollision || mc.player.collidedSoftly;
    }

    private enum SprintMode
    {
        LEGIT,
        RAGE
    }
}

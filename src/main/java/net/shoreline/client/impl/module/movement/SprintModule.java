package net.shoreline.client.impl.module.movement;

import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.rotation.ClientRotationEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.PlayerJumpEvent;
import net.shoreline.client.impl.event.network.StopSprintingEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.util.Formatter;
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
        if (checkNull() || !canSprint())
        {
            return;
        }

        switch (modeConfig.getValue())
        {
            case LEGIT ->
            {
                if (mc.player.input.hasForwardMovement()
                        && (!mc.player.horizontalCollision || mc.player.collidedSoftly))
                {
                    mc.player.setSprinting(true);
                }
            }
            case RAGE -> mc.player.setSprinting(true);
        }
    }

    @EventListener
    public void onClientRotation(ClientRotationEvent event)
    {
        if (!rotateConfig.getValue())
        {
            return;
        }
        float sprintYaw = getYawFromInput();
        event.cancel();
        event.setYaw(sprintYaw);
    }

    @EventListener
    public void onStopSprinting(StopSprintingEvent event)
    {
        if (canSprint() && modeConfig.getValue() == SprintMode.RAGE)
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

            event.cancel();
            event.setYaw(yaw);
        }
    }

    private boolean canSprint()
    {
        boolean inWeb = BlockPos.stream(mc.player.getBoundingBox())
                .anyMatch(p -> mc.world.getBlockState(p).getBlock().equals(Blocks.COBWEB));
        return isInputtingMovement()
                && !inWeb
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isGliding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0f;
    }

    private enum SprintMode
    {
        LEGIT,
        RAGE
    }
}

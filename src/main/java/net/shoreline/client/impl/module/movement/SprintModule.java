package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.network.SprintCancelEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.event.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class SprintModule extends RotationModule
{
    //
    Config<SprintMode> modeConfig = register(new EnumConfig<>("Mode", "Sprinting mode. Rage allows for multi-directional sprinting.", SprintMode.LEGIT, SprintMode.values()));

    /**
     *
     */
    public SprintModule()
    {
        super("Sprint", "Automatically sprints", ModuleCategory.MOVEMENT, 10);
    }

    @Override
    public String getModuleData()
    {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        if (MovementUtil.isInputtingMovement()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F)
        {
            float sprintYaw = getSprintYaw(mc.player.getYaw());
            if (checkSprintAngle(sprintYaw))
            {
                return;
            }
            switch (modeConfig.getValue())
            {
                case LEGIT ->
                {
                    if (mc.player.input.hasForwardMovement()
                            && (!mc.player.horizontalCollision
                            || mc.player.collidedSoftly))
                    {
                        mc.player.setSprinting(true);
                    }
                }
                case RAGE, RAGE_STRICT, GRIM -> mc.player.setSprinting(true);
            }
        }
    }

    @EventListener
    public void onSprintCancel(SprintCancelEvent event)
    {
        if (MovementUtil.isInputtingMovement()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F
                && (modeConfig.getValue() == SprintMode.RAGE || modeConfig.getValue() == SprintMode.RAGE_STRICT))
        {
            float sprintYaw = getSprintYaw(mc.player.getYaw());
            if (checkSprintAngle(sprintYaw))
            {
                return;
            }
            event.cancel();
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (MovementUtil.isInputtingMovement()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F
                && modeConfig.getValue() == SprintMode.RAGE_STRICT)
        {
            float sprintYaw = getSprintYaw(mc.player.getYaw());
            if (checkSprintAngle(sprintYaw))
            {
                return;
            }
            setRotation(sprintYaw, mc.player.getPitch());
        }
    }

    private boolean checkSprintAngle(float sprintYaw)
    {
        if (modeConfig.getValue() != SprintMode.RAGE_STRICT || modeConfig.getValue() != SprintMode.GRIM)
        {
            return false;
        }
        return Managers.ROTATION.isRotating() && !isRotationBlocked() && MathHelper.angleBetween(
                modeConfig.getValue() == SprintMode.GRIM ? mc.player.getYaw() : sprintYaw, Managers.ROTATION.getRotationYaw()) <= 40.0f;
    }

    private float getSprintYaw(float yaw)
    {
        boolean forward = mc.options.forwardKey.isPressed();
        boolean backward = mc.options.backKey.isPressed();
        boolean left = mc.options.leftKey.isPressed();
        boolean right = mc.options.rightKey.isPressed();
        if (forward && !backward)
        {
            if (left && !right)
            {
                yaw -= 45.0f;
            }
            else if (right && !left)
            {
                yaw += 45.0f;
            }
        }
        else if (backward && !forward)
        {
            yaw += 180.0f;
            if (left && !right)
            {
                yaw += 45.0f;
            }
            else if (right && !left)
            {
                yaw -= 45.0f;
            }
        }
        else if (left && !right)
        {
            yaw -= 90.0f;
        }
        else if (right && !left)
        {
            yaw += 90.0f;
        }
        return MathHelper.wrapDegrees(yaw);
    }

    public enum SprintMode
    {
        LEGIT,
        RAGE,
        RAGE_STRICT,
        GRIM
    }
}

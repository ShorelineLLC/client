package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.JumpRotationEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.network.SprintCancelEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.client.RotationsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class SprintModule extends RotationModule
{
    private static SprintModule INSTANCE;

    //
    Config<SprintMode> modeConfig = register(new EnumConfig<>("Mode", "Sprinting mode. Rage allows for multi-directional sprinting.", SprintMode.LEGIT, SprintMode.values()));
    Config<Boolean> jumpFixConfig = register(new BooleanConfig("JumpFix", "Fixes jumping slowdown in Rage sprint", true, () -> modeConfig.getValue() == SprintMode.RAGE || modeConfig.getValue() == SprintMode.RAGE_STRICT));

    /**
     *
     */
    public SprintModule()
    {
        super("Sprint", "Automatically sprints", ModuleCategory.MOVEMENT, 110);
        INSTANCE = this;
    }

    public static SprintModule getInstance()
    {
        return INSTANCE;
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
        if (canSprint())
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
        if (canSprint() && (modeConfig.getValue() == SprintMode.RAGE || modeConfig.getValue() == SprintMode.RAGE_STRICT))
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
        if (canSprint() && modeConfig.getValue() == SprintMode.RAGE_STRICT)
        {
            setRotation(getSprintYaw(mc.player.getYaw()), mc.player.getPitch());
        }
    }

    @EventListener
    public void onJumpYaw(JumpRotationEvent event)
    {
        if (jumpFixConfig.getValue() && (modeConfig.getValue() == SprintMode.RAGE || modeConfig.getValue() == SprintMode.RAGE_STRICT))
        {
            float yaw = event.getYaw();
            float forward = Math.signum(mc.player.input.movementForward);
            float strafe = 90.0f * Math.signum(mc.player.input.movementSideways);
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
        if (AnticheatModule.getInstance().getWebJumpFix() && PlayerUtil.inWeb(1.0))
        {
            return false;
        }
        return MovementUtil.isInputtingMovement()
                && !mc.player.isSneaking()
                && !mc.player.isRiding()
                && !mc.player.isFallFlying()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F;
    }

    private boolean checkSprintAngle(float sprintYaw)
    {
        if (modeConfig.getValue() == SprintMode.RAGE_STRICT)
        {
            return MathHelper.angleBetween(sprintYaw, Managers.ROTATION.getServerYaw()) > 0.0f;
        }
        else if (modeConfig.getValue() == SprintMode.GRIM)
        {
            return MathHelper.angleBetween(mc.player.getYaw(), Managers.ROTATION.getServerYaw()) > 0.0f;
        }
        return false;
    }

    public float getSprintYaw(float yaw)
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

package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.effect.StatusEffects;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.SprintCancelEvent;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.event.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class SprintModule extends ToggleModule
{
    //
    Config<SprintMode> modeConfig = register(new EnumConfig<>("Mode", "Sprinting mode. Rage allows for multi-directional sprinting.", SprintMode.LEGIT, SprintMode.values()));

    /**
     *
     */
    public SprintModule()
    {
        super("Sprint", "Automatically sprints", ModuleCategory.MOVEMENT);
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
                case RAGE -> mc.player.setSprinting(true);
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
                && modeConfig.getValue() == SprintMode.RAGE)
        {
            event.cancel();
        }
    }

    public enum SprintMode
    {
        LEGIT,
        RAGE
    }
}

package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class NoAccelModule extends MovementModule
{
    Config<Boolean> airConfig = new BooleanConfig.Builder("Air")
            .setDescription("Allows instant acceleration in the air")
            .setDefaultValue(false).build();
    Config<Boolean> waterConfig = new BooleanConfig.Builder("Water")
            .setDescription("Allows instant acceleration in water")
            .setDefaultValue(false).build();
    Config<Boolean> fallingConfig = new BooleanConfig.Builder("Falling")
            .setDescription("Allows instant acceleration while falling")
            .setDefaultValue(false).build();

    public NoAccelModule()
    {
        super("NoAccel", "Always sprint at max speed", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (checkNull() || event.getType() != MovementType.SELF)
        {
            return;
        }

        if (!mc.player.isOnGround() && !airConfig.getValue()
                || !mc.player.isOnGround() && mc.player.getVelocity().y < 0.0 && !fallingConfig.getValue()
                || mc.player.isTouchingWater() && !waterConfig.getValue()
                || !InputUtil.isInputtingMovement())
        {
            return;
        }

        double speedEffect = 1.0;
        double slowEffect = 1.0;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
        {
            double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            speedEffect = 1.0f + (0.2 * (amplifier + 1));
        }

        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
        {
            double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            slowEffect = 1.0f + (0.2 * (amplifier + 1));
        }

        final double base = 0.2873f * speedEffect / slowEffect;
        Vec2f motion = strafe((float) base);
        event.cancel();
        event.setMovement(new Vec3d(motion.x, event.getMovement().y, motion.y));
    }
}

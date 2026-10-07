package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.MouseEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.impl.event.render.CameraEvent;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class ElytraFlyModule extends Toggleable
{
    Config<Mode> mode = new EnumConfig.Builder<Mode>("Mode")
            .setValues(Mode.values())
            .setDefaultValue(Mode.CONTROL)
            .build();
    Config<Double> hSpeed = new NumberConfig.Builder<Double>("H-Speed")
            .setMin(0.1).setMax(10.).setDefaultValue(2.5).build();
    Config<Double> vSpeed = new NumberConfig.Builder<Double>("V-Speed")
            .setMin(0.1).setMax(5.).setDefaultValue(1.).build();
    Config<Float> pitch = new NumberConfig.Builder<Float>("Pitch")
            .setMin(70.0f).setMax(90.0f).setDefaultValue(80.0f).build();

    private float cameraPitch;

    public ElytraFlyModule()
    {
        super("ElytraFly", "Fly while wearing elytra", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onMouse(MouseEvent event)
    {
        if (mode.getValue() == Mode.BOUNCE)
        {
            event.cancel();
            float f = (float) event.getCursorDeltaY() * 0.15F;
            float g = (float) event.getCursorDeltaX() * 0.15F;
            this.cameraPitch += f;
            mc.player.setYaw(mc.player.getYaw() + g);
            this.cameraPitch = MathHelper.clamp(cameraPitch, -90.0F, 90.0F);
        }
    }

    @EventListener
    public void onRotation(ClientRotationEvent event)
    {
        if (checkNull() || !mc.player.isGliding())
        {
            return;
        }

        if (mode.getValue() == Mode.BOUNCE)
        {
            mc.player.setPitch(pitch.getValue());
            event.cancel();
            event.setPitch(pitch.getValue());
        }
    }

    @EventListener
    public void onCameraRotation(CameraEvent.Rotation event)
    {
        if (mode.getValue() == Mode.BOUNCE)
        {
            event.cancel();
            event.setPitch(cameraPitch);
        }
    }

    @EventListener
    public void onTravel_Pre(TravelEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        switch (mode.getValue())
        {
            case CONTROL ->
            {
                if (!mc.player.isGliding())
                {
                    return;
                }

                event.cancel();
                double forward  = mc.player.input.movementForward;
                double sideways = mc.player.input.movementSideways;
                double velocityX = 0.0;
                double velocityY = 0.0;
                double velocityZ = 0.0;

                if (forward != 0.0 || sideways != 0.0)
                {
                    float yaw = Managers.ROTATION.hasClientRotation()
                            ? Managers.ROTATION.getClientRotation().getYaw()
                            : mc.player.getYaw();

                    double rad = Math.toRadians(yaw + 90.0f);
                    double cos = Math.cos(rad);
                    double sin = Math.sin(rad);

                    double speed = hSpeed.getValue();
                    velocityX = (forward * speed * cos) + (sideways * speed * sin);
                    velocityZ = (forward * speed * sin) - (sideways * speed * cos);
                }

                if (mc.options.jumpKey.isPressed())
                {
                    velocityY = vSpeed.getValue();
                }
                else if (mc.options.sneakKey.isPressed())
                {
                    velocityY = -vSpeed.getValue();
                }

                Vec3d velocity = new Vec3d(velocityX, velocityY, velocityZ);
                mc.player.setVelocity(velocity);
                mc.player.move(MovementType.SELF, velocity);
            }
            case BOUNCE ->
            {
                if (!mc.player.isGliding())
                {
                    sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startGliding();
                }

                if (canSprint())
                {
                    mc.player.setSprinting(true);
                }

                if (mc.player.isOnGround())
                {
                    mc.player.jump();
                }
            }
        }
    }

    private boolean canSprint()
    {
        return !mc.player.isSneaking() && !mc.player.isRiding()
                && !mc.player.isTouchingWater()
                && !mc.player.isInLava()
                && !mc.player.isHoldingOntoLadder()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6.0F;
    }

    private enum Mode
    {
        CONTROL,
        BOUNCE
    }
}

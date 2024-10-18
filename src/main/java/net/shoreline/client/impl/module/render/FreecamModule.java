package net.shoreline.client.impl.module.render;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.MacroConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.MouseUpdateEvent;
import net.shoreline.client.impl.event.PerspectiveEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.camera.CameraPositionEvent;
import net.shoreline.client.impl.event.camera.CameraRotationEvent;
import net.shoreline.client.impl.event.camera.EntityCameraPositionEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.entity.EntityRotationVectorEvent;
import net.shoreline.client.impl.event.keyboard.KeyboardInputEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.render.BobViewEvent;
import net.shoreline.client.impl.event.render.item.RenderFirstPersonEvent;
import net.shoreline.client.impl.manager.player.rotation.Rotation;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.RayCastUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.lwjgl.glfw.GLFW;

/**
 * @author auto
 * @since 1.0
 */
public class FreecamModule extends ToggleModule
{
    private static FreecamModule INSTANCE;

    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The move speed of the camera", 0.1f, 4.0f, 10.0f));
    Config<Macro> controlConfig = register(new MacroConfig("ControlKey", "", new Macro(getId() + "-control", GLFW.GLFW_KEY_LEFT_ALT, () -> {})));
    Config<Boolean> toggleControlConfig = register(new BooleanConfig("ToggleControl", "Allows toggling control key instead of holding", false));
    Config<Interact> interactConfig = register(new EnumConfig<>("Interact", "The interaction type of the camera", Interact.CAMERA, Interact.values()));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotate to the point of interaction", false));

    public Vec3d position, lastPosition;

    public float yaw, pitch;

    public boolean control = false;

    public FreecamModule()
    {
        super("Freecam", "Allows you to control the camera separately from the player", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static FreecamModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    protected void onEnable()
    {
        if (mc.player == null) return;
        control = false;

        position = mc.gameRenderer.getCamera().getPos();
        lastPosition = position;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        mc.player.input = new FreecamKeyboardInput(mc.options);
    }

    @Override
    protected void onDisable()
    {
        if (mc.player == null) return;
        mc.player.input = new KeyboardInput(mc.options);
    }

    @EventListener
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            disable();
        }
    }

    @EventListener
    public void onKey(KeyboardInputEvent event)
    {
        // Do nothing for GLFW_REPEAT
        if (event.getAction() != GLFW.GLFW_REPEAT && event.getKeycode() == controlConfig.getValue().getKeycode())
        {
            if (!toggleControlConfig.getValue())
            {
                control = event.getAction() == GLFW.GLFW_PRESS;
            }
            else
            {
                if (event.getAction() == GLFW.GLFW_PRESS)
                {
                    control = !control;
                }
            }
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        disable();
    }

    @EventListener
    public void onCameraPosition(CameraPositionEvent event)
    {
        event.setPosition(control ? position : lastPosition.lerp(position, event.getTickDelta()));
    }

    @EventListener
    public void onCameraRotation(CameraRotationEvent event)
    {
        event.setRotation(new Vec2f(yaw, pitch));
    }

    @EventListener
    public void onMouseUpdate(MouseUpdateEvent event)
    {
        if (!control)
        {
            event.cancel();
            changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
        }
    }

    @EventListener
    public void onEntityCameraPosition(EntityCameraPositionEvent event)
    {
        if (event.getEntity() != mc.player) return;
        if (!control && interactConfig.getValue() == Interact.CAMERA)
        {
            event.setPosition(position);
        }
    }

    @EventListener
    public void onEntityRotation(EntityRotationVectorEvent event)
    {
        if (event.getEntity() != mc.player) return;
        if (!control && interactConfig.getValue() == Interact.CAMERA)
        {
            event.setPosition(RotationUtil.getRotationVector(pitch, yaw));
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE) return;
        if (!control && rotateConfig.getValue())
        {
            float[] currentAngles = {yaw, pitch};
            Vec3d eyePos = position;
            HitResult result = RayCastUtil.rayCast(mc.player.getBlockInteractionRange(), eyePos, currentAngles);
            if (result.getType() == HitResult.Type.BLOCK)
            {
                float[] newAngles = RotationUtil.getRotationsTo(mc.player.getEyePos(), result.getPos());
                Managers.ROTATION.setRotation(new Rotation(1, newAngles[0], newAngles[1]));
            }
        }
    }

    // Render the player in third person
    @EventListener
    public void onPerspective(PerspectiveEvent event)
    {
        event.cancel();
    }

    @EventListener
    public void onRenderArm(RenderFirstPersonEvent.Head event)
    {
        event.cancel();
    }

    @EventListener
    public void onBob(BobViewEvent event)
    {
        if (control) event.cancel();
    }

    public class FreecamKeyboardInput extends KeyboardInput
    {

        private final GameOptions options;

        public FreecamKeyboardInput(GameOptions options)
        {
            super(options);
            this.options = options;
        }

        @Override
        public void tick(boolean slowDown, float slowDownFactor)
        {
            if (control)
            {
                super.tick(slowDown, slowDownFactor);
            }
            else
            {
                unset();
                float speed = speedConfig.getValue() / 10f;
                float fakeMovementForward = getMovementMultiplier(options.forwardKey.isPressed(), options.backKey.isPressed());
                float fakeMovementSideways = getMovementMultiplier(options.leftKey.isPressed(), options.rightKey.isPressed());
                Vec2f dir = handleVanillaMotion(speed, fakeMovementForward, fakeMovementSideways);

                float y = 0;
                if (options.jumpKey.isPressed())
                {
                    y += speed;
                }
                else if (options.sneakKey.isPressed())
                {
                    y -= speed;
                }

                lastPosition = position;
                position = position.add(dir.x, y, dir.y);
            }
        }

        private void unset()
        {
            this.pressingForward = false;
            this.pressingBack = false;
            this.pressingLeft = false;
            this.pressingRight = false;
            this.movementForward = 0;
            this.movementSideways = 0;
            this.jumping = false;
            this.sneaking = false;
        }
    }

    /**
     * @see KeyboardInput#getMovementMultiplier(boolean, boolean)
     */
    private float getMovementMultiplier(boolean positive, boolean negative)
    {
        if (positive == negative)
        {
            return 0.0F;
        }
        else
        {
            return positive ? 1.0F : -1.0F;
        }
    }

    /**
     * Modified version of {@link net.shoreline.client.impl.module.movement.SpeedModule#handleVanillaMotion(float)}
     */
    private Vec2f handleVanillaMotion(final float speed, float forward, float strafe)
    {
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        }
        else if (forward != 0.0f && strafe != 0.0f)
        {
            forward *= (float) Math.sin(0.7853981633974483);
            strafe *= (float) Math.cos(0.7853981633974483);
        }
        return new Vec2f((float) (forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw))),
                (float) (forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw))));
    }

    /**
     * @param cursorDeltaX
     * @param cursorDeltaY
     * @see net.minecraft.entity.Entity#changeLookDirection(double, double)
     */
    private void changeLookDirection(double cursorDeltaX, double cursorDeltaY)
    {
        float f = (float) cursorDeltaY * 0.15F;
        float g = (float) cursorDeltaX * 0.15F;
        this.pitch += f;
        this.yaw += g;
        this.pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
    }

    public Vec3d getCameraPosition()
    {
        return position;
    }

    public Vec3d getLastCameraPosition()
    {
        return lastPosition;
    }

    public float[] getCameraRotations()
    {
        return new float[]{yaw, pitch};
    }

    public enum Interact
    {
        PLAYER,
        CAMERA
    }
}


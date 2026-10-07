package net.shoreline.client.impl.module.render;

import lombok.Getter;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.MouseEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.entity.PlayerVecEvent;
import net.shoreline.client.impl.event.render.CameraEvent;
import net.shoreline.client.impl.event.render.RenderPlayerThirdPersonEvent;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.world.RaytraceUtil;
import net.shoreline.eventbus.annotation.EventListener;

/** @author auto **/
@Getter
public class FreecamModule extends Toggleable
{
    public static FreecamModule INSTANCE;

    Config<Float> speedConfig = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.1f).setMax(10.0f).setDefaultValue(5.0f)
            .setDescription("The camera move speed").build();
    Config<Interact> interactConfig = new EnumConfig.Builder<Interact>("Interact")
            .setValues(Interact.values()).setDefaultValue(Interact.CAMERA)
            .setDescription("Where to interact from").build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates the player before interacting")
            .setVisible(() -> interactConfig.getValue() == Interact.CAMERA)
            .setDefaultValue(false).build();

    private Vec3d position, lastPosition;
    private float yaw, pitch;

    public FreecamModule()
    {
        super("Freecam", "Look around freely", GuiCategory.RENDER);
        INSTANCE = this;
    }

    @Override
    protected void onEnable()
    {
        if (checkNull())
        {
            return;
        }

        position = mc.gameRenderer.getCamera().getPos();
        lastPosition = position;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        mc.player.input = new FreecamKeyboardInput(mc.options);
    }

    @Override
    protected void onDisable()
    {
        if (checkNull())
        {
            return;
        }

        mc.player.input = new KeyboardInput(mc.options);
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener
    public void onCameraPosition(CameraEvent.Position event)
    {
        final Vec3d pos = lastPosition.lerp(position, event.getTickDelta());

        event.cancel();
        event.setX(pos.x);
        event.setY(pos.y);
        event.setZ(pos.z);
    }

    @EventListener
    public void onCameraRotation(CameraEvent.Rotation event)
    {
        event.cancel();
        event.setYaw(yaw);
        event.setPitch(pitch);
    }

    @EventListener
    public void onMouseUpdate(MouseEvent event)
    {
        event.cancel();
        changeLookDirection(event.getCursorDeltaX(), event.getCursorDeltaY());
    }

    @EventListener
    public void onEntityCameraPosition(PlayerVecEvent.Camera event)
    {
        if (interactConfig.getValue() == Interact.CAMERA)
        {
            event.cancel();
            event.setVec(position);
        }
    }

    @EventListener
    public void onEntityRotation(PlayerVecEvent.Rotation event)
    {
        if (interactConfig.getValue() == Interact.CAMERA)
        {
            event.cancel();
            event.setVec(RotationUtil.getRotationVector(yaw, pitch));
        }
    }

    @EventListener
    public void onRenderPlayerThirdPerson(RenderPlayerThirdPersonEvent event)
    {
        event.cancel();
    }

    @EventListener
    public void onClientRotation(ClientRotationEvent event)
    {
        if (event.isCanceled())
        {
            return;
        }

        if (rotateConfig.getValue())
        {
            float[] currentAngles = { yaw, pitch };
            Vec3d eyePos = position;
            HitResult result = RaytraceUtil.raycast(mc.player.getBlockInteractionRange(), eyePos, currentAngles);
            if (result instanceof BlockHitResult blockResult)
            {
                float[] newAngles = RotationUtil.getRotationsTo(mc.player.getEyePos(), blockResult.getBlockPos().toCenterPos());
                event.cancel();
                event.setYaw(newAngles[0]);
                event.setPitch(newAngles[1]);
            }
        }
    }

    @EventListener
    public void onRenderHeldItem(RenderHeldItemEvent.Pre event)
    {
        event.cancel();
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
        } else if (forward != 0.0f && strafe != 0.0f)
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

    public enum Interact
    {
        PLAYER,
        CAMERA
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
        public void tick()
        {
            unset();
            float speed = speedConfig.getValue() / 10.0f;
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

        private void unset()
        {
            this.playerInput = new PlayerInput(false, false, false, false, false, false, false);
            this.movementForward = 0;
            this.movementSideways = 0;
        }
    }
}

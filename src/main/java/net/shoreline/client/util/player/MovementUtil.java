package net.shoreline.client.util.player;

import net.minecraft.client.input.Input;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.util.Globals;

/**
 * @author linus
 * @since 1.0
 */
public class MovementUtil implements Globals
{
    /**
     * @return
     */
    public static boolean isInputtingMovement()
    {
        return mc.options.forwardKey.isPressed()
                || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed()
                || mc.options.rightKey.isPressed();
    }

    /**
     * @return
     */
    public static boolean isMovingInput()
    {
        return mc.player.input.movementForward != 0.0f
                || mc.player.input.movementSideways != 0.0f;
    }

    /**
     * @return
     */
    public static boolean isMoving()
    {
        double d = mc.player.getX() - mc.player.lastX;
        double e = mc.player.getY() - mc.player.lastBaseY;
        double f = mc.player.getZ() - mc.player.lastZ;
        return MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0e-4);
    }

    public static void applySneak()
    {
        final float modifier = MathHelper.clamp(0.3f + (EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.SWIFT_SNEAK) * 0.15F), 0.0f, 1.0f);
        mc.player.input.movementForward *= modifier;
        mc.player.input.movementSideways *= modifier;
    }

    public static Vec2f applySafewalk(final double motionX, final double motionZ)
    {
        final double offset = 0.05;

        double moveX = motionX;
        double moveZ = motionZ;

        float fallDist = -mc.player.getStepHeight();
        if (!mc.player.isOnGround())
        {
            fallDist = -1.5f;
        }

        while (moveX != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(moveX, fallDist, 0.0)))
        {
            if (moveX < offset && moveX >= -offset)
            {
                moveX = 0.0;
            }
            else if (moveX > 0.0)
            {
                moveX -= offset;
            }
            else
            {
                moveX += offset;
            }
        }

        while (moveZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0.0, fallDist, moveZ)))
        {
            if (moveZ < offset && moveZ >= -offset)
            {
                moveZ = 0.0;
            }
            else if (moveZ > 0.0)
            {
                moveZ -= offset;
            }
            else
            {
                moveZ += offset;
            }
        }

        while (moveX != 0.0 && moveZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(moveX, fallDist, moveZ)))
        {
            if (moveX < offset && moveX >= -offset)
            {
                moveX = 0.0;
            }
            else if (moveX > 0.0)
            {
                moveX -= offset;
            }
            else
            {
                moveX += offset;
            }

            if (moveZ < offset && moveZ >= -offset)
            {
                moveZ = 0.0;
            }
            else if (moveZ > 0.0)
            {
                moveZ -= offset;
            }
            else
            {
                moveZ += offset;
            }
        }

        return new Vec2f((float) moveX, (float) moveZ);

    }

    public static float getYawOffset(Input input, float rotationYaw)
    {
        if (input.movementForward < 0.0f) rotationYaw += 180.0f;

        float forward = 1.0f;
        if (input.movementForward < 0.0f)
        {
            forward = -0.5f;
        }
        else if (input.movementForward > 0.0f)
        {
            forward = 0.5f;
        }

        float strafe = input.movementSideways;
        if (strafe > 0.0f) rotationYaw -= 90.0f * forward;
        if (strafe < 0.0f) rotationYaw += 90.0f * forward;
        return rotationYaw;
    }
}

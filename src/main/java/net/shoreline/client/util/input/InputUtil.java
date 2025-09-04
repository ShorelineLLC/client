package net.shoreline.client.util.input;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

@UtilityClass
public class InputUtil
{
    private final GameOptions options = MinecraftClient.getInstance().options;

    public PlayerInput inputJumping(PlayerInput playerInput, boolean jumping)
    {
        return input(playerInput, jumping, playerInput.sneak(), playerInput.sprint());
    }

    public PlayerInput inputSneaking(PlayerInput playerInput, boolean sneaking)
    {
        return input(playerInput, playerInput.jump(), sneaking, playerInput.sprint());
    }

    public PlayerInput inputSprinting(PlayerInput playerInput, boolean sprinting)
    {
        return input(playerInput, playerInput.jump(), playerInput.sneak(), sprinting);
    }

    public PlayerInput input(PlayerInput playerInput, boolean jumping, boolean sneaking, boolean sprinting)
    {
        return new PlayerInput(playerInput.forward(),
                playerInput.backward(),
                playerInput.left(),
                playerInput.right(),
                jumping,
                sneaking,
                sprinting);
    }

    public boolean isInputtingHotbar()
    {
        for (KeyBinding binding : options.hotbarKeys)
        {
            if (binding.isPressed())
            {
                return true;
            }
        }

        return false;
    }

    public KeyBinding[] getMovementKeys()
    {
        return new KeyBinding[] {
                options.forwardKey,
                options.backKey,
                options.leftKey,
                options.rightKey
        };
    }

    public boolean isInputtingMovement()
    {
        return Arrays.stream(getMovementKeys()).anyMatch(KeyBinding::isPressed);
    }

    public Direction getDirectionFromInput(Direction facing)
    {
        boolean forward = options.forwardKey.isPressed();
        boolean backward = options.backKey.isPressed();
        boolean left = options.leftKey.isPressed();
        boolean right = options.rightKey.isPressed();

        if (forward && !backward)
        {
            return facing;
        } else if (backward && !forward)
        {
            return facing.getOpposite();
        }

        if (left && !right)
        {
            return facing.rotateYClockwise();
        } else if (right && !left)
        {
            return facing.rotateYCounterclockwise();
        }

        return null;
    }

    public float getYawFromInput(float yaw)
    {
        boolean forward = options.forwardKey.isPressed();
        boolean backward = options.backKey.isPressed();
        boolean left = options.leftKey.isPressed();
        boolean right = options.rightKey.isPressed();

        if (forward && !backward)
        {
            if (left && !right)
            {
                yaw -= 45.0f;
            } else if (right && !left)
            {
                yaw += 45.0f;
            }
        } else if (backward && !forward)
        {
            yaw += 180.0f;
            if (left && !right)
            {
                yaw += 45.0f;
            } else if (right && !left)
            {
                yaw -= 45.0f;
            }
        } else if (left && !right)
        {
            yaw -= 90.0f;
        } else if (right && !left)
        {
            yaw += 90.0f;
        }

        return MathHelper.wrapDegrees(yaw);
    }
}

package net.shoreline.client.util.input;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.PlayerInput;

@UtilityClass
public class InputUtil
{
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

    public boolean isInputtingMovement()
    {
        return MinecraftClient.getInstance().options.forwardKey.isPressed()
                || MinecraftClient.getInstance().options.backKey.isPressed()
                || MinecraftClient.getInstance().options.leftKey.isPressed()
                || MinecraftClient.getInstance().options.rightKey.isPressed();
    }
}

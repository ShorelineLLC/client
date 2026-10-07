package net.shoreline.client.mixin.input;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Input.class)
public class MixinInput
{
    @Shadow public float movementSideways;
    @Shadow public float movementForward;
}

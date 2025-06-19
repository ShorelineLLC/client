package net.shoreline.client.mixin.accessor;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Input.class)
public interface AccessorInput
{
    @Accessor
    void setMovementVector(Vec2f movementVector);
}

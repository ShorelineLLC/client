package net.shoreline.client.mixin.render.block;

import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FluidRenderer.class, priority = Integer.MAX_VALUE)
public class MixinFluidRenderer
{
    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "CONSTANT",
                    args = "intValue=16",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            ordinal = 0
    )
    private int hookI(int i)
    {
        // TODO: Figure out why this works for fabric but not us????
        return i;
    }
}

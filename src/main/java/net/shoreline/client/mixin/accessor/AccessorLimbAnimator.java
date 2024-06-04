package net.shoreline.client.mixin.accessor;

import net.minecraft.entity.LimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LimbAnimator.class)
public interface AccessorLimbAnimator
{

    @Accessor("pos")
    void hookSetPos(float pos);

    @Accessor("speed")
    void hookSetSpeed(float speed);
}

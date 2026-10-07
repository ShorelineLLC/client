package net.shoreline.client.mixin.render.entity;

import net.minecraft.entity.LimbAnimator;
import net.shoreline.client.impl.imixin.ILimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LimbAnimator.class)
public abstract class MixinLimbAnimator implements ILimbAnimator
{
    @Override
    @Accessor(value = "pos")
    public abstract void setPos(float pos);
}

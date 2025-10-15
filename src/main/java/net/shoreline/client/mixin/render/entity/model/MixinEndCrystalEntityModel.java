package net.shoreline.client.mixin.render.entity.model;

import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.model.EndCrystalEntityModel;
import net.shoreline.client.impl.module.render.ModelsModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndCrystalEntityModel.class)
public class MixinEndCrystalEntityModel
{
    @ModifyConstant(
            method = "setAngles(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;)V",
            constant = @Constant(floatValue = 3.0f),
            require = 1
    )
    private float hookAngles(float constant)
    {
        return ModelsModule.INSTANCE.isEnabled() ? ModelsModule.INSTANCE.getCrystalSpin().getValue() * constant : constant;
    }

    @Redirect(
            method = "setAngles(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EndCrystalEntityRenderer;getYOffset(F)F"))
    private float hookYOffset(float f)
    {
        return ModelsModule.INSTANCE.isEnabled() && ModelsModule.INSTANCE.getCrystalBounce().getValue() ? EndCrystalEntityRenderer.getYOffset(f) : -1.1f;
    }
}

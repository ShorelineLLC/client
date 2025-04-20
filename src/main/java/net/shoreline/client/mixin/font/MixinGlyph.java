package net.shoreline.client.mixin.font;

import net.minecraft.client.font.Glyph;
import net.shoreline.client.impl.module.client.FontModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Glyph.class)
public interface MixinGlyph
{
    @Inject(method = "getShadowOffset", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetShadowOffset(CallbackInfoReturnable<Float> cir)
    {
        cir.cancel();
        cir.setReturnValue(FontModule.getInstance().getVanillaShadow());
    }
}

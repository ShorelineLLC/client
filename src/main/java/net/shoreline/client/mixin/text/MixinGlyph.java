package net.shoreline.client.mixin.text;

import net.minecraft.client.font.Glyph;
import net.shoreline.client.impl.event.render.GlyphShadowEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Glyph.class)
public interface MixinGlyph
{
    @Inject(method = "getShadowOffset",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetShadowOffset(CallbackInfoReturnable<Float> cir)
    {
        GlyphShadowEvent event = new GlyphShadowEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(event.getShadowOffset());
        }
    }
}

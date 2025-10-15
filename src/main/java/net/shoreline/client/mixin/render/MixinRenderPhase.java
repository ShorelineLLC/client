package net.shoreline.client.mixin.render;

import net.minecraft.client.render.RenderPhase;
import net.shoreline.client.impl.event.render.GlintTextureEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPhase.class)
public class MixinRenderPhase
{
    @Unique
    private boolean isGlintTexturing;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void init(String name, Runnable beginAction, Runnable endAction, CallbackInfo ci)
    {
        this.isGlintTexturing = name.contains("glint_texturing");
    }

    @Inject(
            method = "startDrawing",
            at = @At("TAIL")
    )
    private void startDraw(CallbackInfo ci)
    {
        if (isGlintTexturing)
        {
            EventBus.INSTANCE.dispatch(new GlintTextureEvent.Pre());
        }
    }

    @Inject(
            method = "endDrawing",
            at = @At("TAIL")
    )
    private void endDrawing(CallbackInfo p1)
    {
        if (isGlintTexturing)
        {
            EventBus.INSTANCE.dispatch(new GlintTextureEvent.Post());
        }
    }
}

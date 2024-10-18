package net.shoreline.client.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.render.RenderFogEvent;
import net.shoreline.client.impl.event.world.BlindnessEvent;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer
{

    @Shadow
    private static float red;

    @Shadow
    private static float green;

    @Shadow
    private static float blue;

    /**
     * @param camera
     * @param fogType
     * @param viewDistance
     * @param thickFog
     * @param tickDelta
     * @param ci
     */
    @Inject(method = "applyFog", at = @At(value = "TAIL"))
    private static void hookApplyFog(Camera camera, BackgroundRenderer.FogType fogType,
                                     float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci)
    {
        if (fogType != BackgroundRenderer.FogType.FOG_TERRAIN)
        {
            return;
        }
        RenderFogEvent renderFogEvent = new RenderFogEvent(viewDistance);
        EventBus.INSTANCE.dispatch(renderFogEvent);
        if (renderFogEvent.isCanceled())
        {
            RenderSystem.setShaderFogStart(renderFogEvent.getStart());
            RenderSystem.setShaderFogEnd(renderFogEvent.getEnd());
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRender(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci)
    {
        SkyboxEvent.Fog skyboxEvent = new SkyboxEvent.Fog(tickDelta);
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            ci.cancel();
            Vec3d vec3d = skyboxEvent.getColorVec();
            red = (float) vec3d.x;
            green = (float) vec3d.y;
            blue = (float) vec3d.z;
            RenderSystem.clearColor(red, green, blue, 0.0f);
        }
    }

    @Inject(method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/" +
            "BackgroundRenderer$StatusEffectFogModifier;", at = @At("HEAD"), cancellable = true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<BackgroundRenderer.StatusEffectFogModifier> cir)
    {
        BlindnessEvent blindnessEvent = new BlindnessEvent();
        EventBus.INSTANCE.dispatch(blindnessEvent);
        if (blindnessEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(null);
        }
    }
}

package net.shoreline.client.mixin.render;

import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.shoreline.client.impl.event.render.NightVisionEvent;
import net.shoreline.client.impl.event.render.WorldGammaEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager
{
    @Shadow
    @Final
    private SimpleFramebuffer lightmapFramebuffer;

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V"))
    private void hookEndWrite(float delta, CallbackInfo info)
    {
        final WorldGammaEvent event = new WorldGammaEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            lightmapFramebuffer.clear();
        }
    }

    @Redirect(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private boolean hookUpdate(ClientPlayerEntity instance, RegistryEntry registryEntry)
    {
        NightVisionEvent nightVisionEvent = new NightVisionEvent();
        EventBus.INSTANCE.dispatch(nightVisionEvent);
        return nightVisionEvent.isCanceled() || instance.hasStatusEffect(registryEntry);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F"))
    private float hookUpdate$2(LivingEntity entity, float tickDelta)
    {
        NightVisionEvent nightVisionEvent = new NightVisionEvent();
        EventBus.INSTANCE.dispatch(nightVisionEvent);
        return nightVisionEvent.isCanceled() ? 1.0f : GameRenderer.getNightVisionStrength(entity, tickDelta);
    }
}

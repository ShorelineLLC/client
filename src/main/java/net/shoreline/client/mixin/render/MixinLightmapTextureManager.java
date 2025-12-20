package net.shoreline.client.mixin.render;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.shoreline.client.impl.event.render.BlockLightEvent;
import net.shoreline.client.impl.event.render.NightVisionEvent;
import net.shoreline.client.impl.event.render.WorldGammaEvent;
import net.shoreline.client.impl.event.render.WorldTintEvent;
import net.shoreline.client.impl.render.Programs;
import net.shoreline.eventbus.EventBus;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private ShaderProgram lightmapProgram;

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Lnet/minecraft/client/gl/ShaderProgramKey;)Lnet/minecraft/client/gl/ShaderProgram;"
            )
    )
    private ShaderProgram hookUpdateLightmap(ShaderProgramKey shaderProgramKey)
    {
        lightmapProgram = RenderSystem.setShader(Programs.LIGHTMAP_KEY);
        return lightmapProgram;
    }

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Lnet/minecraft/client/gl/ShaderProgramKey;)Lnet/minecraft/client/gl/ShaderProgram;",
                    shift = At.Shift.AFTER
            )
    )
    private void hookSetUniforms(float delta, CallbackInfo ci)
    {
        if (lightmapProgram != null)
        {
            WorldTintEvent.Light worldTintEvent = new WorldTintEvent.Light();
            EventBus.INSTANCE.dispatch(worldTintEvent);
            Vector3f color = worldTintEvent.getColorVec3();

            lightmapProgram.getUniformOrDefault("CustomLightColor").set(color.x, color.y, color.z);
            lightmapProgram.getUniformOrDefault("CustomLightStrength").set(worldTintEvent.isCanceled() ? 1.0f : 0.0f);
        }
    }

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

    @WrapMethod(method = "getBlockLightCoordinates")
    private static int getBlockLightCoordinatesHook(int light, Operation<Integer> original)
    {
        BlockLightEvent event = new BlockLightEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return event.getBlockLight();
        }

        return original.call(light);
    }
}

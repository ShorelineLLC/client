package net.shoreline.client.mixin.texture;

import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.LightmapTextureManager;
import net.shoreline.client.impl.event.render.WorldTintEvent;
import net.shoreline.eventbus.EventBus;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public abstract class MixinLightmapTextureManager
{
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Uniform;set(Lorg/joml/Vector3f;)V"))
    public void updateHook(Uniform instance, Vector3f vector)
    {
        WorldTintEvent.Light lightTint = new WorldTintEvent.Light();
        EventBus.INSTANCE.dispatch(lightTint);
        if (lightTint.isCanceled())
        {
            instance.set(lightTint.getColorVec3());
            return;
        }

        instance.set(vector);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Uniform;set(F)V", ordinal = 2))
    public void updateHook$3(Uniform instance, float value1)
    {
        WorldTintEvent.Light lightTint = new WorldTintEvent.Light();
        EventBus.INSTANCE.dispatch(lightTint);
        if (lightTint.isCanceled())
        {
            instance.set(1.0f);
            return;
        }

        instance.set(value1);
    }
}
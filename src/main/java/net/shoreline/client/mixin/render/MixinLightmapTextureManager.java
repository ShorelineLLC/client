package net.shoreline.client.mixin.render;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.render.AmbientColorEvent;
import net.shoreline.client.impl.event.render.LightmapGammaEvent;
import net.shoreline.eventbus.bus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

/**
 * @author linus
 * @see LightmapTextureManager
 * @since 1.0
 */
@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    //
    @Shadow
    @Final
    private NativeImage image;

    /**
     * @param args
     */
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void hookUpdate(Args args) {
        LightmapGammaEvent lightmapGammaEvent =
                new LightmapGammaEvent(args.get(2));
        EventBus.INSTANCE.dispatch(lightmapGammaEvent);
        if (lightmapGammaEvent.isCanceled()) {
            args.set(2, lightmapGammaEvent.getGamma());
        }
    }

    /**
     * @param delta
     * @param ci
     */
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/client/texture/NativeImageBackedTexture;upload()V",
            shift = At.Shift.BEFORE))
    private void hookUpdate(float delta, CallbackInfo ci) {
        final AmbientColorEvent ambientColorEvent = new AmbientColorEvent();
        EventBus.INSTANCE.dispatch(ambientColorEvent);
        if (ambientColorEvent.isCanceled()) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    int color = image.getColor(i, j);
                    int[] bgr = new int[]
                            {
                                    color >> 16 & 255, color >> 8 & 255,
                                    color & 255
                            };
                    Vec3d colors = new Vec3d(bgr[2] / 255.0, bgr[1] / 255.0,
                            bgr[0] / 255.0);
                    final Color c = ambientColorEvent.getColor();
                    Vec3d ncolors = new Vec3d(c.getRed() / 255.0,
                            c.getGreen() / 255.0, c.getBlue() / 255.0);
                    Vec3d mix = mix(colors, ncolors, c.getAlpha() / 255.0);
                    int r = (int) (mix.x * 255.0);
                    int g = (int) (mix.y * 255.0);
                    int b = (int) (mix.z * 255.0);
                    image.setColor(i, j, -16777216 | r << 16 | g << 8 | b);
                }
            }
        }
    }

    private Vec3d mix(Vec3d first, Vec3d second, double factor) {
        return new Vec3d(first.x * (1.0f - factor) + second.x * factor,
                first.y * (1.0f - factor) + second.y * factor,
                first.z * (1.0f - factor) + first.z * factor);
    }
}

package net.shoreline.client.mixin.text;

import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextColor.class)
public class MixinTextColor
{
    @Final
    @Shadow
    @Mutable
    private int rgb;

    @Inject(method = "<init>(I)V", at = @At(value = "TAIL"))
    private void init(int rgb, CallbackInfo ci)
    {
        this.rgb = rgb;
    }

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At(value = "TAIL"))
    private void init(int rgb, String name, CallbackInfo ci)
    {
        this.rgb = rgb;
    }
}

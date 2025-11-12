package net.shoreline.client.mixin.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexConsumers.class)
public class MixinVertexConsumers
{
    /* @Inject(
            method = "union(Lnet/minecraft/client/render/VertexConsumer;" +
                    "Lnet/minecraft/client/render/VertexConsumer;" +
                    ")Lnet/minecraft/client/render/VertexConsumer;",
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void unionHook_Duplicate(VertexConsumer first, VertexConsumer second, CallbackInfoReturnable<VertexConsumer> cir)
    {
        cir.setReturnValue(new ClientDual(first, second)); // get rid of retarded error.
        cir.cancel();
    } */
}

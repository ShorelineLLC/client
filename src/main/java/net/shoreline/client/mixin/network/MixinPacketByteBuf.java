package net.shoreline.client.mixin.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf
{
    @Shadow
    @Nullable
    public abstract NbtElement readNbt(NbtSizeTracker sizeTracker);

    /**
     * @param ops
     * @param codec
     * @param sizeTracker
     * @param cir
     */
    @Inject(method = "decode(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/" +
            "serialization/Codec;Lnet/minecraft/nbt/NbtSizeTracker;)Ljava/lang/Object;",
            at = @At(value = "HEAD"), cancellable = true)
    private void hookDecode(DynamicOps<NbtElement> ops, Codec<Object> codec,
                            NbtSizeTracker sizeTracker, CallbackInfoReturnable<Object> cir)
    {
        cir.cancel();
        try
        {
            NbtElement nbtElement = readNbt(sizeTracker);
            cir.setReturnValue(Util.getResult(codec.parse(ops, nbtElement), error -> new DecoderException("Failed to decode: " + error + " " + nbtElement)));
        }
        catch (DecoderException e)
        {
            cir.setReturnValue(null);
        }
    }
}

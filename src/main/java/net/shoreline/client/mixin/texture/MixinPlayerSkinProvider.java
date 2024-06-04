package net.shoreline.client.mixin.texture;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerSkinProvider.FileCache.class)
public class MixinPlayerSkinProvider
{

    @Shadow
    @Final
    private MinecraftProfileTexture.Type type;

//    /**
//     *
//     * @param hash
//     * @param cir
//     */
//    @Inject(method = "getTexturePath", at = @At(value = "HEAD"), cancellable = true)
//    private void hookGetTexturePath(String hash, CallbackInfoReturnable<Identifier> cir) {
//        Identifier capeTexture = Managers.CAPES.getCapeTexture();
//        if (type == MinecraftProfileTexture.Type.CAPE && capeTexture != null) {
//            cir.cancel();
//            cir.setReturnValue(capeTexture);
//        }
//    }
}

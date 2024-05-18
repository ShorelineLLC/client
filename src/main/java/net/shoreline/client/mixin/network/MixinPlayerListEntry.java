package net.shoreline.client.mixin.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.event.buffers.ManagersBuffer;
import net.shoreline.client.impl.event.buffers.ModulesBuffer;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry implements Globals {

    @Unique
    private Identifier capeTexture;
    @Unique
    private boolean capeTextureLoaded;

    /**
     *
     * @param profile
     * @param secureChatEnforced
     * @param ci
     */
    @Inject(method = "<init>(Lcom/mojang/authlib/GameProfile;Z)V", at = @At("TAIL"))
    private void hookInit(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
        if (capeTextureLoaded) {
            return;
        }
        ManagersBuffer.getCapesManager().loadPlayerCape(profile, identifier -> {
            capeTexture = identifier;
        });
        capeTextureLoaded = true;
    }

    /**
     *
     * @param cir
     */
    @Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
    private void hookGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        if (capeTexture != null && (ModulesBuffer.getCapesModule().isEnabled() && ModulesBuffer.getCapesModule().getOptifineConfig().getValue())) {
            SkinTextures t = cir.getReturnValue();
            SkinTextures customCapeTexture = new SkinTextures(t.texture(), t.textureUrl(), capeTexture, capeTexture, t.model(), t.secure());
            cir.setReturnValue(customCapeTexture);
        }
    }
}

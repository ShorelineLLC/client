package net.shoreline.client.mixin.render.item;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(BuiltinModelItemRenderer.class)
public class MixinBuiltinModelItemRenderer
{
    @Unique
    private static final Map<SkullBlock.SkullType, Identifier> TEXTURES = Util.make(Maps.newHashMap(), (map) -> {
        map.put(SkullBlock.Type.SKELETON, new Identifier("textures/entity/skeleton/skeleton.png"));
        map.put(SkullBlock.Type.WITHER_SKELETON, new Identifier("textures/entity/skeleton/wither_skeleton.png"));
        map.put(SkullBlock.Type.ZOMBIE, new Identifier("textures/entity/zombie/zombie.png"));
        map.put(SkullBlock.Type.CREEPER, new Identifier("textures/entity/creeper/creeper.png"));
        map.put(SkullBlock.Type.DRAGON, new Identifier("textures/entity/enderdragon/dragon.png"));
        map.put(SkullBlock.Type.PIGLIN, new Identifier("textures/entity/piglin/piglin.png"));
        map.put(SkullBlock.Type.PLAYER, DefaultSkinHelper.getTexture());
    });

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/render/RenderLayer;"))
    public RenderLayer hookGetRenderLayer(SkullBlock.SkullType type, GameProfile profile)
    {
        Identifier identifier = TEXTURES.get(type);
        if (type == SkullBlock.Type.PLAYER && profile != null)
        {
            PlayerSkinProvider playerSkinProvider = MinecraftClient.getInstance().getSkinProvider();
            return RenderLayersClient.ENTITY_TRANSLUCENT.apply(playerSkinProvider.getSkinTextures(profile).texture(), true);
        }
        else
        {
            return RenderLayersClient.ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(identifier, true);
        }
    }
}

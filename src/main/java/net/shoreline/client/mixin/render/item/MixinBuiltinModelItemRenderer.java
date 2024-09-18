package net.shoreline.client.mixin.render.item;

import com.google.common.collect.Maps;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.component.type.ProfileComponent;
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
    private static final Map<SkullBlock.SkullType, Identifier> TEXTURES = Util.make(Maps.newHashMap(), (map) ->
    {
        map.put(SkullBlock.Type.SKELETON, Identifier.of("textures/entity/skeleton/skeleton.png"));
        map.put(SkullBlock.Type.WITHER_SKELETON, Identifier.of("textures/entity/skeleton/wither_skeleton.png"));
        map.put(SkullBlock.Type.ZOMBIE, Identifier.of("textures/entity/zombie/zombie.png"));
        map.put(SkullBlock.Type.CREEPER, Identifier.of("textures/entity/creeper/creeper.png"));
        map.put(SkullBlock.Type.DRAGON, Identifier.of("textures/entity/enderdragon/dragon.png"));
        map.put(SkullBlock.Type.PIGLIN, Identifier.of("textures/entity/piglin/piglin.png"));
        map.put(SkullBlock.Type.PLAYER, DefaultSkinHelper.getTexture());
    });

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lnet/minecraft/component/type/ProfileComponent;)Lnet/minecraft/client/render/RenderLayer;"))
    public RenderLayer hookGetRenderLayer(SkullBlock.SkullType type, ProfileComponent profile)
    {
        Identifier identifier = TEXTURES.get(type);
        if (type == SkullBlock.Type.PLAYER && profile != null)
        {
            PlayerSkinProvider playerSkinProvider = MinecraftClient.getInstance().getSkinProvider();
            return RenderLayersClient.ENTITY_TRANSLUCENT.apply(playerSkinProvider.getSkinTextures(profile.gameProfile()).texture(), true);
        }
        else
        {
            return RenderLayersClient.ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(identifier, true);
        }
    }
}

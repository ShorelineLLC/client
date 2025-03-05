package net.shoreline.client.mixin.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public abstract class MixinRenderLayers
{
    @Shadow
    public static RenderLayer getBlockLayer(BlockState state)
    {
        return null;
    }

    @Inject(method = "getEntityBlockLayer", at = @At(value = "HEAD"), cancellable = true)
    private static void hookGetEntityBlockLayer(BlockState state, boolean direct, CallbackInfoReturnable<RenderLayer> cir)
    {
//        if (!(state.getBlock() instanceof AbstractBannerBlock || state.getBlock() instanceof BedBlock || state.isOf(Blocks.CONDUIT)
//                || state.isOf(Blocks.CHEST) || state.isOf(Blocks.ENDER_CHEST) || state.isOf(Blocks.TRAPPED_CHEST) || state.isOf(Blocks.DECORATED_POT) || state.getBlock() instanceof ShulkerBoxBlock))
//        {
//            return;
//        }
    }

    @Inject(method = "getItemLayer", at = @At(value = "HEAD"), cancellable = true)
    private static void hookGetItemLayer(ItemStack stack, boolean direct, CallbackInfoReturnable<RenderLayer> cir)
    {
        cir.cancel();
        Item item = stack.getItem();
        if (item instanceof BlockItem)
        {
            Block block = ((BlockItem) item).getBlock();
            RenderLayer renderLayer = getBlockLayer(block.getDefaultState());
            if (renderLayer == RenderLayer.getTranslucent())
            {
                if (!MinecraftClient.isFabulousGraphicsOrBetter())
                {
                    cir.setReturnValue(RenderLayersClient.ENTITY_TRANSLUCENT_CULL.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
                }
                else
                {
                    cir.setReturnValue(direct ? RenderLayersClient.ENTITY_TRANSLUCENT_CULL.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE) : RenderLayersClient.ITEM_ENTITY_TRANSLUCENT_CULL_2.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
                }
            }
            else
            {
                cir.setReturnValue(RenderLayersClient.ENTITY_CUTOUT.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
            }
        }
        else
        {
            cir.setReturnValue(direct ? RenderLayersClient.ENTITY_TRANSLUCENT_CULL.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE) : RenderLayersClient.ITEM_ENTITY_TRANSLUCENT_CULL_2.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        }
    }
}

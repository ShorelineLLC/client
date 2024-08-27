package net.shoreline.client.mixin.accessor;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface AccessorItemRenderer
{
    /**
     * @return
     */
    @Accessor("builtinModelItemRenderer")
    BuiltinModelItemRenderer hookGetBuiltinModelItemRenderer();

    @Accessor("colors")
    ItemColors hookGetItemColors();

    @Invoker("renderBakedItemModel")
    void hookRenderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices);
}

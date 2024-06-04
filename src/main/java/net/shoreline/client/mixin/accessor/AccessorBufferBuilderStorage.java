package net.shoreline.client.mixin.accessor;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilderStorage.class)
public interface AccessorBufferBuilderStorage
{
    /**
     * @param vertexConsumerProvider
     */
    @Accessor("entityVertexConsumers")
    @Mutable
    void hookSetEntityVertexConsumers(VertexConsumerProvider.Immediate vertexConsumerProvider);
}

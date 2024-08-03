package net.shoreline.client.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(WorldRenderer.class)
public interface AccessorWorldRenderer
{
    /**
     * @return
     */
    @Accessor("frustum")
    Frustum getFrustum();

    /**
     * @return
     */
    @Accessor("blockBreakingInfos")
    Int2ObjectMap<BlockBreakingInfo> getBlockBreakingProgressions();

    @Accessor("bufferBuilders")
    BufferBuilderStorage hookGetBufferBuilders();
}

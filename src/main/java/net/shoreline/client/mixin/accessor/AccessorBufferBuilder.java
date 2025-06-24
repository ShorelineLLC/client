package net.shoreline.client.mixin.accessor;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferBuilder.class)
public interface AccessorBufferBuilder
{
    @Accessor("vertexPointer")
    long getVertexPointer();

    @Accessor("vertexFormat")
    VertexFormat getVertexFormat();

    @Invoker("beginVertex")
    long beginNewVertex();
}

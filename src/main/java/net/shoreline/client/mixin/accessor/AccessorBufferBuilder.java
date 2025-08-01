package net.shoreline.client.mixin.accessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferBuilder.class)
public interface AccessorBufferBuilder
{
    @Accessor("vertexPointer")
    long getVertexPointer();

    @Accessor("format")
    VertexFormat getVertexFormat();

    @Invoker("beginVertex")
    long beginNewVertex();
}

package net.shoreline.client.mixin.accessor;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawContext.class)
public interface AccessorDrawContext
{
    @Accessor("scissorStack")
    DrawContext.ScissorStack getScissorStack();
}

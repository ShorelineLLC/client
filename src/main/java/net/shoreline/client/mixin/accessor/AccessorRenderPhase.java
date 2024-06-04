package net.shoreline.client.mixin.accessor;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface AccessorRenderPhase
{

    @Mutable
    @Accessor("beginAction")
    void hookSetBeginAction(Runnable runnable);

    @Mutable
    @Accessor("endAction")
    void hookSetEndAction(Runnable runnable);
}

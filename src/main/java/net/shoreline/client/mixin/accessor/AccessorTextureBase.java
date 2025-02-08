package net.shoreline.client.mixin.accessor;


import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(RenderPhase.TextureBase.class)
public interface AccessorTextureBase
{
    @Invoker("getId")
    Optional<Identifier> hookGetId();
}

package net.shoreline.client.mixin.render;

import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.imixin.ITextureBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(RenderPhase.TextureBase.class)
public abstract class MixinTextureBase implements ITextureBase
{
    @Override
    @Invoker("getId")
    public abstract Optional<Identifier> hookGetId();
}

package net.shoreline.client.mixin.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.shoreline.client.impl.imixin.IDrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext implements IDrawContext
{
    @Override
    @Accessor(value = "vertexConsumers")
    public abstract VertexConsumerProvider.Immediate getVertexConsumerProvider();
}

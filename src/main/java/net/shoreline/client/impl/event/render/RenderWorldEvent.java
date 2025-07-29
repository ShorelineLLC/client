package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
public class RenderWorldEvent extends Event
{
    private final MatrixStack matrixStack;
    private final VertexConsumerProvider.Immediate immediate;

    @Cancelable
    public static class Post extends RenderWorldEvent
    {
        public Post(MatrixStack matrixStack,
                    VertexConsumerProvider.Immediate immediate)
        {
            super(matrixStack, immediate);
        }
    }
}

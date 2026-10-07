package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
public class RenderWorldEvent extends Event
{
    private final MatrixStack matrixStack;
    private final float tickDelta;

    @Cancelable
    public static class Post extends RenderWorldEvent
    {
        public Post(MatrixStack matrixStack, float tickDelta)
        {
            super(matrixStack, tickDelta);
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class Resized extends Event
    {
        private final int width, height;
    }
}

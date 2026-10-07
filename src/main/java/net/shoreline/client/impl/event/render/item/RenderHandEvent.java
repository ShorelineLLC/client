package net.shoreline.client.impl.event.render.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@AllArgsConstructor
@Cancelable
@Getter
@Setter
public class RenderHandEvent extends Event
{
    private VertexConsumerProvider vertexConsumerProvider;

    public static class Post extends Event {}
}

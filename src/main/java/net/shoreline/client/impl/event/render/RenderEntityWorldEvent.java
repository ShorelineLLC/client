package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class RenderEntityWorldEvent extends Event
{
    private final Entity entity;
    private VertexConsumerProvider vertexConsumerProvider;

    public RenderEntityWorldEvent(Entity entity, VertexConsumerProvider vertexConsumerProvider)
    {
        this.entity = entity;
        this.vertexConsumerProvider = vertexConsumerProvider;
    }

    public static class Post extends Event {}
}

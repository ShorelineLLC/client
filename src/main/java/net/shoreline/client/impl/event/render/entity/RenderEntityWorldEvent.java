package net.shoreline.client.impl.event.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.shoreline.eventbus.event.Event;

public class RenderEntityWorldEvent extends Event
{
    public final Entity entity;
    public final double cameraX;
    public final double cameraY;
    public final double cameraZ;
    public final float yaw;
    public final float tickDelta;
    public final MatrixStack matrices;
    public final VertexConsumerProvider vertexConsumers;

    public RenderEntityWorldEvent(Entity entity, double cameraX, double cameraY, double cameraZ, float yaw,
                                  float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers)
    {
        this.entity = entity;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.yaw = yaw;
        this.tickDelta = tickDelta;
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
    }
}

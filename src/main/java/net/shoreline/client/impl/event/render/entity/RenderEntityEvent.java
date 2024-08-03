package net.shoreline.client.impl.event.render.entity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

import java.util.List;

@Cancelable
public class RenderEntityEvent<T extends LivingEntity> extends Event
{
    public final LivingEntityRenderer<T, EntityModel<T>> renderer;
    public final LivingEntity entity;

    public final float f;
    public final float g;
    public final MatrixStack matrixStack;
    public final VertexConsumerProvider vertexConsumerProvider;
    public final int i;
    public final RenderLayer layer;
    public final EntityModel model;
    //
    public final List<FeatureRenderer<T, EntityModel<T>>> features;

    /**
     * @param entity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param model
     * @param features
     */
    public RenderEntityEvent(LivingEntityRenderer<T, EntityModel<T>> renderer, LivingEntity entity,
                             float f, float g, MatrixStack matrixStack,
                             VertexConsumerProvider vertexConsumerProvider,
                             int i, EntityModel model, RenderLayer layer,
                             List<FeatureRenderer<T, EntityModel<T>>> features)
    {
        this.renderer = renderer;
        this.entity = entity;
        this.f = f;
        this.g = g;
        this.matrixStack = matrixStack;
        this.vertexConsumerProvider = vertexConsumerProvider;
        this.i = i;
        this.model = model;
        this.layer = layer;
        this.features = features;
    }
}

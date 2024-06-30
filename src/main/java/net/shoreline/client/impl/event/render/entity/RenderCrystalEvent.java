package net.shoreline.client.impl.event.render.entity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RenderCrystalEvent extends Event
{
    // ??
    public final EndCrystalEntity endCrystalEntity;
    public final float f;
    public final float g;
    public final MatrixStack matrixStack;
    public final int i;
    public final ModelPart core;
    public final ModelPart frame;
    //
    public float spin = 1.0f;
    public float scale = 1.0f;
    public boolean bounce = true;

    /**
     * @param endCrystalEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param i
     * @param core
     * @param frame
     */
    public RenderCrystalEvent(EndCrystalEntity endCrystalEntity, float f, float g,
                              MatrixStack matrixStack, int i, ModelPart core, ModelPart frame)
    {
        this.endCrystalEntity = endCrystalEntity;
        this.f = f;
        this.g = g;
        this.matrixStack = matrixStack;
        this.i = i;
        this.core = core;
        this.frame = frame;
    }

    public float getSpin()
    {
        return spin;
    }

    public float getScale()
    {
        return scale;
    }

    public boolean getBounce()
    {
        return bounce;
    }

    public void setSpin(float spin)
    {
        this.spin = spin;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    public void setBounce(boolean bounce)
    {
        this.bounce = bounce;
    }
}


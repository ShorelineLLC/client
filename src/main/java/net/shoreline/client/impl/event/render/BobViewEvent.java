package net.shoreline.client.impl.event.render;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class BobViewEvent extends Event
{

    private final MatrixStack matrixStack;
    private final float tickDelta;
    private float y;

    public BobViewEvent(MatrixStack matrixStack, float tickDelta)
    {
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public MatrixStack getMatrixStack()
    {
        return matrixStack;
    }

    public float getTickDelta()
    {
        return tickDelta;
    }
}

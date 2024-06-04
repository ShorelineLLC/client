package net.shoreline.client.impl.event.render;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.eventbus.Event;

public class ReloadShaderEvent extends Event
{

    private final MatrixStack matrixStack;
    private final float delta;

    public ReloadShaderEvent(MatrixStack matrixStack, float delta)
    {
        this.matrixStack = matrixStack;
        this.delta = delta;
    }

    public MatrixStack getMatrixStack()
    {
        return matrixStack;
    }

    public float getDelta()
    {
        return delta;
    }
}

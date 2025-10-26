package net.shoreline.client.impl.module.render;

import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class ViewModelModule extends Toggleable
{
    Config<Float> translateX = new NumberConfig.Builder<Float>("X")
            .setMin(-3.0f).setMax(3.0f).setDefaultValue(0.0f)
            .setDescription("Translation in x-direction").build();
    Config<Float> translateY = new NumberConfig.Builder<Float>("Y")
            .setMin(-3.0f).setMax(3.0f).setDefaultValue(0.0f)
            .setDescription("Translation in y-direction").build();
    Config<Float> translateZ = new NumberConfig.Builder<Float>("Z")
            .setMin(-3.0f).setMax(3.0f).setDefaultValue(0.0f)
            .setDescription("Translation in z-direction").build();
    Config<Float> scaleX = new NumberConfig.Builder<Float>("ScaleX")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f)
            .setDescription("Scale in x-direction").build();
    Config<Float> scaleY = new NumberConfig.Builder<Float>("ScaleY")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f)
            .setDescription("Scale in y-direction").build();
    Config<Float> scaleZ = new NumberConfig.Builder<Float>("ScaleZ")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f)
            .setDescription("Scale in z-direction").build();
    Config<Float> rotateX = new NumberConfig.Builder<Float>("RotateX")
            .setMin(-180.0f).setMax(180.0f).setDefaultValue(0.0f)
            .setDescription("Rotation in x-direction").build();
    Config<Float> rotateY = new NumberConfig.Builder<Float>("RotateY")
            .setMin(-180.0f).setMax(180.0f).setDefaultValue(0.0f)
            .setDescription("Rotation in y-direction").build();
    Config<Float> rotateZ = new NumberConfig.Builder<Float>("RotateZ")
            .setMin(-180.0f).setMax(180.0f).setDefaultValue(0.0f)
            .setDescription("Rotation in z-direction").build();
    Config<Float> eatingY = new NumberConfig.Builder<Float>("EatingFactor")
            .setMin(0.1f).setMax(1.0f).setDefaultValue(1.0f)
            .setDescription("Eating factor in y-direction").build();
    Config<Integer> eatingDuration = new NumberConfig.Builder<Integer>("EatingDuration")
            .setMin(1).setMax(10).setDefaultValue(4)
            .setDescription("Eating duration length").build();

    public ViewModelModule()
    {
        super("ViewModel", "Changes the hand viewmodel", GuiCategory.RENDER);
    }

    @EventListener
    public void onRenderFirstPerson(RenderHeldItemEvent.FirstPerson event)
    {
        event.getMatrixStack().scale(scaleX.getValue(), scaleY.getValue(), scaleZ.getValue());
        event.getMatrixStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateX.getValue()));
        if (event.getHand() == Hand.MAIN_HAND)
        {
            event.getMatrixStack().translate(translateX.getValue(), translateY.getValue(), translateZ.getValue());
            event.getMatrixStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateY.getValue()));
            event.getMatrixStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotateZ.getValue()));
        } else
        {
            event.getMatrixStack().translate(-translateX.getValue(), translateY.getValue(), translateZ.getValue());
            event.getMatrixStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotateY.getValue()));
            event.getMatrixStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-rotateZ.getValue()));
        }
    }

    @EventListener
    public void onEating(RenderHeldItemEvent.Eating event)
    {
        event.cancel();
        event.setFactorY(eatingY.getValue());
        event.setDuration(eatingDuration.getValue());
    }
}

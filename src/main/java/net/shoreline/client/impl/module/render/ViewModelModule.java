package net.shoreline.client.impl.module.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.config.ToggleableConfigGroup;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.eventbus.annotation.EventListener;
import org.joml.Quaternionf;

public class ViewModelModule extends Toggleable
{
    Config<Float> xTranslate = new NumberConfig.Builder<Float>("X-Translate")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Translation for the X-Axis").build();
    Config<Float> yTranslate = new NumberConfig.Builder<Float>("Y-Translate")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Translation for the Y-Axis").build();
    Config<Float> zTranslate = new NumberConfig.Builder<Float>("Z-Translate")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Translation for the Z-Axis").build();
    Config<Boolean> translateGroup = new ToggleableConfigGroup.Builder("Translation")
            .addAll(xTranslate, yTranslate, zTranslate)
            .setDefaultValue(true).build();

    Config<Float> xSize = new NumberConfig.Builder<Float>("X-Size")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Size for the X-Axis").build();
    Config<Float> ySize = new NumberConfig.Builder<Float>("Y-Size")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Size for the X-Axis").build();
    Config<Float> zSize = new NumberConfig.Builder<Float>("Z-Size")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Size for the X-Axis").build();
    Config<Boolean> sizeGroup = new ToggleableConfigGroup.Builder("Size")
            .addAll(xSize, ySize, zSize)
            .setDefaultValue(true).build();

    Config<Float> xScale = new NumberConfig.Builder<Float>("X-Scale")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Scale for the X-Axis").build();
    Config<Float> yScale = new NumberConfig.Builder<Float>("Y-Scale")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Scale for the Y-Axis").build();
    Config<Float> zScale = new NumberConfig.Builder<Float>("Z-Scale")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setDescription("Scale for the Z-Axis").build();
    Config<Boolean> scaleGroup = new ToggleableConfigGroup.Builder("Scale")
            .addAll(xScale, yScale, zScale)
            .setDefaultValue(true).build();

    Config<Float> xRotate = new NumberConfig.Builder<Float>("X-Rotation")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Rotation for the X-Axis").build();
    Config<Float> yRotate = new NumberConfig.Builder<Float>("Y-Rotation")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Rotation for the Y-Axis").build();
    Config<Float> zRotate = new NumberConfig.Builder<Float>("Z-Rotation")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Rotation for the Z-Axis").build();
    Config<Boolean> rotationGroup = new ToggleableConfigGroup.Builder("Rotation")
            .addAll(xRotate, yRotate, zRotate)
            .setDefaultValue(true).build();

    Config<Float> mainX = new NumberConfig.Builder<Float>("MainHand-X")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
                .setDescription("Translation for Mainhand X").build();
    Config<Float> mainY = new NumberConfig.Builder<Float>("MainHand-Y")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Translation for Mainhand Y").build();
    Config<Boolean> mainhandGroup = new ToggleableConfigGroup.Builder("Mainhand")
            .addAll(mainX, mainY)
            .setDefaultValue(true).build();

    Config<Float> offhandX = new NumberConfig.Builder<Float>("Offhand-X")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Translation for Offhand X").build();
    Config<Float> offhandY = new NumberConfig.Builder<Float>("Offhand-Y")
            .setMin(-5.0f).setMax(5.0f).setDefaultValue(0.0f)
            .setDescription("Translation for Offhand Y").build();
    Config<Boolean> offhandGroup = new ToggleableConfigGroup.Builder("Offhand")
            .addAll(offhandX, offhandY)
            .setDefaultValue(true).build();

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
    public void onRenderFirstPerson_EquipProgress(RenderHeldItemEvent.EquipProgress event)
    {
        float handY = getHandY(event.getHand());
        if (handY == -1000.0f)
        {
            return;
        }

        event.cancel();
        event.setHeight(handY);
    }

    @EventListener
    public void onRenderFirstPerson_SwingProgress(RenderHeldItemEvent.HandSwing event)
    {
        float swingProgress = getHandX(event.getHand());
        if (swingProgress == -1000.0f)
        {
            return;
        }

        event.cancel();
        event.setSwingProgress(swingProgress);
    }

    @EventListener
    public void onRenderFirstPerson_Scaling(RenderHeldItemEvent.Scaling event)
    {
        MatrixStack matrices = event.getMatrixStack();
        if (scaleGroup.getValue())
        {
            matrices.scale(xScale.getValue(), yScale.getValue(), zScale.getValue());
        }

        if (rotationGroup.getValue())
        {
            matrices.multiply(new Quaternionf(xRotate.getValue(), yRotate.getValue(), zRotate.getValue(), 1.0f));
        }
    }

    @EventListener
    public void onRenderFirstPerson_Translation(RenderHeldItemEvent.Translation event)
    {
        if (translateGroup.getValue())
        {
            MatrixStack matrices = event.getMatrixStack();
            matrices.translate(xTranslate.getValue(), yTranslate.getValue(), zTranslate.getValue());
        }
    }

    @EventListener
    public void onRenderFirstPerson_Size(RenderHeldItemEvent.Size event)
    {
        if (sizeGroup.getValue())
        {
            MatrixStack matrices = event.getMatrixStack();
            matrices.scale(xSize.getValue(), ySize.getValue(), zSize.getValue());
        }
    }

    @EventListener
    public void onEating(RenderHeldItemEvent.Eating event)
    {
        event.cancel();
        event.setFactorY(eatingY.getValue());
        event.setDuration(eatingDuration.getValue());
    }

    public float getHandX(Hand hand)
    {
        if (hand == Hand.MAIN_HAND)
        {
            if (!mainhandGroup.getValue())
            {
                return -1000.0f;
            }

            return mainX.getValue();
        }

        if (!offhandGroup.getValue())
        {
            return -1000.0f;
        }

        return offhandX.getValue();
    }

    public float getHandY(Hand hand)
    {
        if (hand == Hand.MAIN_HAND)
        {
            if (!mainhandGroup.getValue())
            {
                return -1000.0f;
            }

            return mainY.getValue();
        }

        if (!offhandGroup.getValue())
        {
            return -1000.0f;
        }

        return offhandY.getValue();
    }
}

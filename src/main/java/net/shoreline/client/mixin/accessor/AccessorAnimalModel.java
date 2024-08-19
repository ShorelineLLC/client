package net.shoreline.client.mixin.accessor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnimalModel.class)
public interface AccessorAnimalModel
{
    @Invoker("getBodyParts")
    Iterable<ModelPart> hookGetBodyParts();

    @Invoker("getHeadParts")
    Iterable<ModelPart> hookGetHeadParts();

    @Accessor("headScaled")
    boolean hookGetHeadScaled();

    @Accessor("invertedChildHeadScale")
    float hookGetInvertedChildHeadScale();

    @Accessor("invertedChildBodyScale")
    float hookGetInvertedChildBodyScale();

    @Accessor("childHeadYOffset")
    float hookGetChildHeadYOffset();

    @Accessor("childHeadZOffset")
    float hookGetChildHeadZOffset();

    @Accessor("childBodyYOffset")
    float hookGetChildBodyYOffset();
}

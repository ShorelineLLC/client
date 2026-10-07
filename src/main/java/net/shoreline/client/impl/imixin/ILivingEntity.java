package net.shoreline.client.impl.imixin;

@IMixin
public interface ILivingEntity
{
    void setLeaningPitch(float leaningPitch);

    void setLastLeaningPitch(float lastLeaningPitch);
}

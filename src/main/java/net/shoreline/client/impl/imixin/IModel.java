package net.shoreline.client.impl.imixin;

import net.minecraft.entity.Entity;

@IMixin
public interface IModel
{
    void cancelModel(boolean cancel);
}

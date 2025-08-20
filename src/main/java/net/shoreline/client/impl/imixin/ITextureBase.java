package net.shoreline.client.impl.imixin;

import net.minecraft.util.Identifier;

import java.util.Optional;

@IMixin
public interface ITextureBase
{
    Optional<Identifier> hookGetId();
}

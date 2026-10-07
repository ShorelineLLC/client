package net.shoreline.client.impl.imixin;

import net.minecraft.entity.player.PlayerModelPart;

import java.util.Set;

@IMixin
public interface IGameOptions
{
    Set<PlayerModelPart> getPlayerModelParts();
}

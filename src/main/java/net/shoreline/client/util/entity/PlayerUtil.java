package net.shoreline.client.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;

@UtilityClass
public class PlayerUtil
{
    public boolean isInSurvival(PlayerEntity player)
    {
        return !player.isCreative() && !player.isSpectator();
    }
}

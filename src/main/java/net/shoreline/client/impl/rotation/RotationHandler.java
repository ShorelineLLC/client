package net.shoreline.client.impl.rotation;

import lombok.Getter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.shoreline.client.impl.Managers;

public class RotationHandler
{
    @Getter
    private Rotation cachedRotation;

    public void applyRotations(ClientPlayerEntity player)
    {
        cachedRotation = new Rotation(player);
        Rotation curr = Managers.ROTATION.getClientRotation();
        curr.apply(player);
    }

    public void revertRotations(ClientPlayerEntity player)
    {
        if (player == null || cachedRotation == null)
        {
            return;
        }

        cachedRotation.apply(player);
        cachedRotation = null;
    }

    public void resetRotations(Rotation playerRotation, float speed)
    {
        if (!Managers.ROTATION.hasClientRotation())
        {
            return;
        }

        Managers.ROTATION.clearClientRotation();
    }
}

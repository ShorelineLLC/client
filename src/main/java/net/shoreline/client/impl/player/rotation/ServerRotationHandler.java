package net.shoreline.client.impl.player.rotation;

import lombok.Getter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;

public class ServerRotationHandler
{
    @Getter
    private Rotation cachedRotation;

    public void onRotationOutbound(PlayerMoveC2SPacket packet)
    {
        if (Managers.ROTATION.hasClientRotation())
        {
            Rotation rotation = Managers.ROTATION.getClientRotation();
            ((AccessorPlayerMoveC2SPacket) packet).setYaw(rotation.getYaw());
            ((AccessorPlayerMoveC2SPacket) packet).setPitch(rotation.getPitch());
        }
    }

    public void onRotationInbound()
    {
        if (!Managers.ROTATION.hasClientRotation())
        {
            return;
        }

        // Rotation rotation = Managers.ROTATION.getClientRotation();
        // rotation.apply(mc.player);
        Managers.ROTATION.clearClientRotation();
    }

    public void onPacketUpdatePre(ClientPlayerEntity player)
    {
        if (player == null || !Managers.ROTATION.hasClientRotation())
        {
            return;
        }

        cachedRotation = new Rotation(player);

        Rotation curr = Managers.ROTATION.getClientRotation();
        curr.apply(player);
    }

    public void onPacketUpdatePost(ClientPlayerEntity player)
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

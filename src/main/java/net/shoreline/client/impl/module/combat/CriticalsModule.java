package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.ac.Anticheat;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2S;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

public class CriticalsModule extends Toggleable
{
    Config<CritMode> modeConfig = new EnumConfig.Builder<CritMode>("Mode")
            .setValues(CritMode.values()).setDescription("The critical attack packet mode")
            .setDefaultValue(CritMode.PACKET).build();

    private boolean postUpdateGround;

    public CriticalsModule()
    {
        super("Criticals", "Always land critical hits", GuiCategory.COMBAT);
    }

    @Override
    public String getModuleData()
    {
        return Formatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable()
    {
        postUpdateGround = false;
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull() || !mc.player.isOnGround())
        {
            return;
        }

        if (event.getPacket() instanceof IPlayerInteractEntityC2S packet)
        {
            final Entity attacked = packet.getEntity(mc.world);
            if (attacked == null || !attacked.isAlive() || !(attacked instanceof LivingEntity))
            {
                return;
            }

            sendCritPackets();
        }
    }

    public void sendCritPackets()
    {
        switch (modeConfig.getValue())
        {
            case PACKET ->
            {
                sendPacketInternal(0.0625f, false);
                sendPacketInternal(0.0f, false);
            }
            case PACKET_STRICT ->
            {
                sendPacketInternal(1.1e-7f, false);
                sendPacketInternal(1.0e-8f, false);
                postUpdateGround = true;
            }
            case GRIM ->
            {
                Rotation playerRotation = Managers.ROTATION.hasClientRotation() ? Managers.ROTATION.getClientRotation() : new Rotation(mc.player);
                float pitch = Math.clamp(playerRotation.getPitch(),
                        -90.0F + Anticheat.GRIM_GCD_DIVISOR,
                        90.0F - Anticheat.GRIM_GCD_DIVISOR);

                sendRotatePacketInternal(0.0625f, playerRotation.getYaw(), pitch + Anticheat.GRIM_GCD_DIVISOR, false);
                sendRotatePacketInternal(0.04535f, playerRotation.getYaw(), pitch - Anticheat.GRIM_GCD_DIVISOR, false);
            }
        }
    }

    private void sendPacketInternal(double yOffset, boolean onGround)
    {
        Packet<?> movePacket = new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(),
                mc.player.getY() + yOffset,
                mc.player.getZ(),
                onGround,
                mc.player.horizontalCollision);

        sendPacket(movePacket);
    }

    private void sendRotatePacketInternal(double yOffset, float yaw, float pitch, boolean onGround)
    {
        Packet<?> movePacket = new PlayerMoveC2SPacket.Full(mc.player.getX(),
                mc.player.getY() + yOffset,
                mc.player.getZ(),
                yaw,
                pitch,
                onGround,
                mc.player.horizontalCollision);

        sendPacket(movePacket);
    }

    public enum CritMode
    {
        PACKET,
        PACKET_STRICT,
        GRIM
    }
}

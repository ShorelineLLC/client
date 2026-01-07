package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.ac.Anticheat;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2S;
import net.shoreline.client.impl.module.combat.util.PhaseUtil;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.client.util.text.Formatter;
import net.shoreline.eventbus.annotation.EventListener;

public class CriticalsModule extends MovementModule
{
    Config<CritMode> modeConfig = new EnumConfig.Builder<CritMode>("Mode")
            .setValues(CritMode.values()).setDescription("The critical attack packet mode")
            .setDefaultValue(CritMode.NCP).build();

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

    @EventListener
    public void onPacketOutbound(PacketEvent.OutboundPost event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket && postUpdateGround)
        {
            sendPacketInternal(0.0f, false);
            postUpdateGround = false;
        }
    }

    public void sendCritPackets()
    {
        switch (modeConfig.getValue())
        {
            case NCP ->
            {
                sendPacketInternal(0.0625f, false);
                sendPacketInternal(0.0f, false);
            }
            case STRICT_NCP ->
            {
                sendPacketInternal(1.1e-7f, false);
                sendPacketInternal(1.0e-8f, false);
                postUpdateGround = true;
            }
            case GRIM ->
            {
                if (mc.player.input.movementForward != 0.0f
                        || mc.player.input.movementSideways != 0.0f
                        || !PhaseUtil.isInsideBlock(mc.player)
                        || !PhaseUtil.isInsideWall(mc.player))
                {
                    return;
                }

                float offset = Anticheat.GCD_DIVISOR;
                Rotation playerRotation = Managers.ROTATION.hasClientRotation() ?
                        Managers.ROTATION.getClientRotation() : new Rotation(mc.player);

                sendRotatePacketInternal(0.0f, playerRotation.getYaw(), playerRotation.getPitch(), true);
                sendRotatePacketInternal(0.0625f, playerRotation.getYaw(),
                        Math.clamp(playerRotation.getPitch() + offset, -90.0f, 90.0f), false);
                sendRotatePacketInternal(0.04535f, playerRotation.getYaw(),
                        Math.clamp(playerRotation.getPitch() - offset, -90.0f, 90.0f), false);
            }
        }
    }

    private void sendPacketInternal(double yOffset, boolean onGround)
    {
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(),
                mc.player.getY() + yOffset,
                mc.player.getZ(),
                onGround,
                mc.player.horizontalCollision));
    }

    private void sendRotatePacketInternal(double yOffset, float yaw, float pitch, boolean onGround)
    {
        sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                mc.player.getY() + yOffset,
                mc.player.getZ(),
                yaw,
                pitch,
                onGround,
                mc.player.horizontalCollision));
    }

    public enum CritMode
    {
        NCP,
        STRICT_NCP,
        GRIM
    }
}

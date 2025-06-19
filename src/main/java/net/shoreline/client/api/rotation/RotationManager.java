package net.shoreline.client.api.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.client.impl.event.render.entity.PlayerTransformsEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
@Setter
public class RotationManager extends GenericFeature
{
    private Rotation clientRotation;

    private final ServerRotationHandler handler;
    private final Rotation serverRotation;

    public RotationManager()
    {
        super("Rotations");
        this.handler = new ServerRotationHandler();
        this.serverRotation = new Rotation();
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onRotationUpdate(RotationUpdateEvent event)
    {
        setClientRotation(new Rotation(event.getYaw(), event.getPitch()));
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket
                || event.getPacket() instanceof EntityS2CPacket packet && packet.getEntity(mc.world) == mc.player
                || event.getPacket() instanceof EntityPositionS2CPacket packet1 && mc.world.getEntityById(packet1.entityId()) == mc.player)
        {
            handler.onRotationInbound();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook())
        {
            serverRotation.setYaw(packet.getYaw(0.0f));
            serverRotation.setPitch(packet.getPitch(0.0f));
            handler.onRotationOutbound(packet);
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onUpdatePre(PlayerUpdateEvent.PrePacket event)
    {
        handler.onPacketUpdatePre();
    }

    @EventListener(priority = Integer.MAX_VALUE)
    public void onUpdatePost(PlayerUpdateEvent.Post event)
    {
        handler.onPacketUpdatePost();
    }

    @EventListener
    public void onUpdatePre(PlayerUpdateEvent.Pre event)
    {
        Rotation playerRotation = new Rotation(mc.player);
        ClientRotationEvent rotationEvent = new ClientRotationEvent(playerRotation);
        EventBus.INSTANCE.dispatch(rotationEvent);
        if (rotationEvent.isCanceled())
        {
            setClientRotation(rotationEvent.getRotation());
        }
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        mc.player.bodyYaw = serverRotation.getYaw();
        mc.player.headYaw = serverRotation.getYaw();
    }

    @EventListener
    public void onPlayerTransforms(PlayerTransformsEvent event)
    {
        if (hasClientRotation())
        {
            event.cancel();
            event.setPitch(clientRotation.getPitch());
        }
    }

    public void clearClientRotation()
    {
        this.clientRotation = null;
    }

    public boolean hasClientRotation()
    {
        return clientRotation != null;
    }
}

package net.shoreline.client.impl.player.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.PlayerJumpEvent;
import net.shoreline.client.impl.event.entity.PlayerVelocityEvent;
import net.shoreline.client.impl.event.input.PlayerInputEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.client.impl.event.render.entity.PlayerTransformsEvent;
import net.shoreline.client.impl.module.client.RotationsModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
@Setter
public class RotationManager extends GenericFeature
{
    private final RotationsModule rotationsConfig = RotationsModule.INSTANCE;

    private Rotation clientRotation;

    private final ServerRotationHandler handler;
    private final MovementCorrection moveFix;
    private final Rotation serverRotation;

    private Rotation preJumpRotation;

    public RotationManager()
    {
        super("Rotations");
        this.handler = new ServerRotationHandler();
        this.moveFix = new MovementCorrection();
        this.serverRotation = new Rotation();
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onRotationUpdate(RotationUpdateEvent event)
    {
        setClientRotation(new Rotation(event.getYaw(), event.getPitch()));
    }

    /** Standard vanilla rotation movement correction **/
    @EventListener
    public void onJumpPre(PlayerJumpEvent.Pre event)
    {
        if (rotationsConfig.shouldApplyMoveFix())
        {
            preJumpRotation = new Rotation(mc.player);
            if (hasClientRotation())
            {
                clientRotation.apply(mc.player);
            }
        }
    }

    @EventListener
    public void onJumpPost(PlayerJumpEvent.Post event)
    {
        if (rotationsConfig.shouldApplyMoveFix())
        {
            preJumpRotation.apply(mc.player);
        }
    }

    @EventListener
    public void onPlayerVelocity(PlayerVelocityEvent event)
    {
        if (hasClientRotation() && rotationsConfig.shouldApplyMoveFix())
        {
            event.cancel();
            event.setYaw(clientRotation.getYaw());
        }
    }

    /** Silent movement correction **/
    @EventListener
    public void onPlayerInput(PlayerInputEvent event)
    {
        if (!checkNull() && hasClientRotation() && rotationsConfig.shouldApplyMoveFix())
        {
            float deltaYaw = mc.player.getYaw() - clientRotation.getYaw();
            final Vec2f corrected = moveFix.correctMovement(rotationsConfig.shouldRoundMoveFix(),
                    deltaYaw, event.getMovementInput().y, event.getMovementInput().x);
            event.cancel();
            event.setMovementInput(corrected);
        }
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
        handler.onPacketUpdatePre(mc.player);
    }

    @EventListener(priority = Integer.MAX_VALUE)
    public void onUpdatePost(PlayerUpdateEvent.Post event)
    {
        handler.onPacketUpdatePost(mc.player);
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
        } else if (hasClientRotation())
        {
            handler.resetRotations(playerRotation, 1.0f);
        }
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (checkNull() || !rotationsConfig.showServerRotation())
        {
            return;
        }

        mc.player.setBodyYaw(serverRotation.getYaw());
        mc.player.setHeadYaw(serverRotation.getYaw());
    }

    @EventListener
    public void onPlayerTransforms(PlayerTransformsEvent event)
    {
        if (rotationsConfig.showServerRotation())
        {
            event.cancel();
            event.setPitch(serverRotation.getPitch());
        }
    }

    /**
     * Should instantly update server rotations
     * @param rotation
     */
    public void setInstantRotation(Rotation rotation)
    {
        setClientRotation(rotation);
        Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                rotation.getYaw(), rotation.getPitch(),
                mc.player.isOnGround(), mc.player.horizontalCollision));
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

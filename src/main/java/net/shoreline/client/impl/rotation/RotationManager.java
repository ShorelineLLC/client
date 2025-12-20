package net.shoreline.client.impl.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.impl.event.entity.PlayerJumpEvent;
import net.shoreline.client.impl.event.entity.PlayerVelocityEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.impl.event.input.PlayerInputEvent;
import net.shoreline.client.impl.event.network.*;
import net.shoreline.client.impl.event.render.entity.PlayerTransformsEvent;
import net.shoreline.client.impl.module.client.RotationsModule;
import net.shoreline.client.impl.module.client.RotationsModule.MoveFix;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.client.impl.render.animation.Smoother;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

@Getter
@Setter
public class RotationManager extends NetworkHandler
{
    private final RotationsModule rotationsConfig = RotationsModule.INSTANCE;

    private Rotation clientRotation;

    private final RotationHandler handler;
    private final MovementCorrection moveFix;
    private final Rotation serverRotation;

    private Rotation preJumpRotation;

    private final Smoother yawAnim = new Smoother();
    private final Smoother pitchAnim = new Smoother();

    public RotationManager()
    {
        super("Rotations");
        this.handler = new RotationHandler();
        this.moveFix = new MovementCorrection();
        this.serverRotation = new Rotation(0.0f, 0.0f);
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onRotationUpdate(RotationUpdateEvent event)
    {
        Rotation rotationUpdate = new Rotation(event.getYaw(), event.getPitch());
        if (!rotationsConfig.getNoServerRotate().getValue())
        {
            setClientRotation(rotationUpdate);
        }

        serverRotation.setYaw(rotationUpdate.getYaw());
        serverRotation.setPitch(rotationUpdate.getPitch());
    }

    /** Standard vanilla rotation movement correction **/
    @EventListener
    public void onJumpPre(PlayerJumpEvent.Pre event)
    {
        if (rotationsConfig.getMoveFixConfig().getValue() != MoveFix.OFF)
        {
            preJumpRotation = new Rotation(mc.player);
            if (hasClientRotation())
            {
                clientRotation.applyToPlayer();
            }
        }
    }

    @EventListener
    public void onJumpPost(PlayerJumpEvent.Post event)
    {
        if (rotationsConfig.getMoveFixConfig().getValue() != MoveFix.OFF)
        {
            preJumpRotation.applyToPlayer();
        }
    }

    @EventListener
    public void onPlayerVelocity(PlayerVelocityEvent event)
    {
        if (hasClientRotation() && rotationsConfig.getMoveFixConfig().getValue() != MoveFix.OFF)
        {
            event.cancel();
            event.setYaw(clientRotation.getYaw());
        }
    }

    /** Silent movement correction **/
    @EventListener
    public void onPlayerInput(PlayerInputEvent event)
    {
        if (!checkNull() && hasClientRotation() && rotationsConfig.getMoveFixConfig().getValue() != MoveFix.OFF)
        {
            float deltaYaw = mc.player.getYaw() - clientRotation.getYaw();
            final Vec2f corrected = moveFix.correctMovement(deltaYaw, event.getMovementForward(), event.getMovementSideways());
            event.cancel();
            event.setMovementForward(corrected.y);
            event.setMovementSideways(corrected.x);
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
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onUpdatePre(PlayerUpdateEvent.PrePacket event)
    {
        if (hasClientRotation())
        {
            handler.applyRotations(mc.player);
        }
    }

    @EventListener(priority = Integer.MAX_VALUE)
    public void onUpdatePost(PlayerUpdateEvent.Post event)
    {
        handler.revertRotations(mc.player);
    }

    @EventListener(priority = Integer.MIN_VALUE)
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

    private static final List<Item> PROJECTILE_ITEMS = List.of(
            Items.SNOWBALL,
            Items.EGG,
            Items.ENDER_PEARL,
            Items.EXPERIENCE_BOTTLE,
            Items.SPLASH_POTION,
            Items.LINGERING_POTION,
            Items.WIND_CHARGE,
            Items.FIRE_CHARGE
    );

    @EventListener
    public void onInteractItem(InteractItemEvent.Pre event)
    {
        if (!PROJECTILE_ITEMS.contains(event.getItem()) || !hasClientRotation())
        {
            return;
        }

        if (rotationsConfig.getItemFixConfig().getValue())
        {
            setSilentRotation(new Rotation(mc.player));
        } else
        {
            handler.applyRotations(mc.player);
        }
    }

    @EventListener
    public void onInteractItem(InteractItemEvent.Post event)
    {
        if (rotationsConfig.getItemFixConfig().getValue() && hasClientRotation())
        {
            resetSilentRotation();
        } else
        {
            handler.revertRotations(mc.player);
        }
    }

    @EventListener
    public void onMovementPackets(MovementPacketsEvent.Update event)
    {
        if (rotationsConfig.getTickSyncConfig().getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onMovementPackets(MovementPacketsEvent.Send event)
    {
        if (rotationsConfig.getLookSyncConfig().getValue())
        {
            if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround posGround)
            {
                event.cancel();
                event.setPacket(new PlayerMoveC2SPacket.Full(posGround.getX(0.0),
                        posGround.getY(0.0),
                        posGround.getZ(0.0),
                        mc.player.getYaw(),
                        mc.player.getPitch(),
                        mc.player.isOnGround(),
                        mc.player.horizontalCollision));
            } else if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround lookGround)
            {
                event.cancel();
                event.setPacket(new PlayerMoveC2SPacket.Full(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        lookGround.getYaw(0.0f),
                        lookGround.getPitch(0.0f),
                        mc.player.isOnGround(),
                        mc.player.horizontalCollision));
            }
        }
    }

    @EventListener
    public void onTravelPre(TravelEvent.Pre event)
    {
        if (rotationsConfig.getFixTravel().getValue() && hasClientRotation())
        {
            handler.applyRotations(mc.player);
        }
    }

    @EventListener
    public void onTravelPost(TravelEvent.Post event)
    {
        if (rotationsConfig.getFixTravel().getValue())
        {
            handler.revertRotations(mc.player);
        }
    }

    @EventListener
    public void onPlayerTransforms(PlayerTransformsEvent event)
    {
        if (rotationsConfig.getRenderRotationsConfig().getValue())
        {
            float yaw = MathHelper.wrapDegrees(serverRotation.getYaw());
            float delta = event.getTickDelta() * 0.05f;
            float renderYaw = (float) yawAnim.smooth(yaw, 5.0, delta);
            float renderPitch = (float) pitchAnim.smooth(serverRotation.getPitch(), 5.0, delta);

            event.cancel();
            event.setYaw(renderYaw);
            event.setPitch(renderPitch);
        }
    }

    /**
     * Should instantly update server rotations
     * @param rotation
     */
    public void setSilentRotation(Rotation rotation)
    {
        if (serverRotation.getYaw() == rotation.getYaw()
                && serverRotation.getPitch() == rotation.getPitch())
        {
            return;
        }

        sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                rotation.getYaw(),
                rotation.getPitch(),
                mc.player.isOnGround(),
                mc.player.horizontalCollision));
    }

    public void resetSilentRotation()
    {
        Rotation playerRotation = hasClientRotation() ? clientRotation : new Rotation(mc.player);
        setSilentRotation(playerRotation);
    }

    public boolean isFacingYaw(float yaw)
    {
        float dyaw = MathHelper.wrapDegrees(serverRotation.getYaw() - yaw);
        return Math.abs(dyaw) <= 0.1f;
    }

    public boolean isFacingPitch(float pitch)
    {
        float p2 = MathHelper.clamp(pitch, -90.0f, 90.0f);
        return Math.abs(serverRotation.getPitch() - p2) <= 0.1f;
    }

    public boolean isFacing(float yaw, float pitch)
    {
        return isFacingYaw(yaw) && isFacingPitch(pitch);
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

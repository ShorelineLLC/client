package net.shoreline.client.impl.module.movement;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.VelocityEvent;
import net.shoreline.client.impl.event.entity.player.PushEntityEvent;
import net.shoreline.client.impl.event.entity.player.PushFluidsEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.network.PushOutOfBlocksEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorClientWorld;
import net.shoreline.client.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket;
import net.shoreline.client.mixin.accessor.AccessorExplosionS2CPacket;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;

/**
 * @author Gavin, linus
 * @since 1.0
 */
public class VelocityModule extends ToggleModule
{
    private static VelocityModule INSTANCE;

    Config<Boolean> knockbackConfig = register(new BooleanConfig("Knockback", "Removes player knockback velocity", true));
    Config<Boolean> explosionConfig = register(new BooleanConfig("Explosion", "Removes player explosion velocity", true));
    Config<VelocityMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for velocity", VelocityMode.NORMAL, VelocityMode.values()));
    Config<Float> horizontalConfig = register(new NumberConfig<>("Horizontal", "How much horizontal knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> modeConfig.getValue() == VelocityMode.NORMAL));
    Config<Float> verticalConfig = register(new NumberConfig<>("Vertical", "How much vertical knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> modeConfig.getValue() == VelocityMode.NORMAL));
    Config<Boolean> pushEntitiesConfig = register(new BooleanConfig("NoPush-Entities", "Prevents being pushed away from entities", true));
    Config<Boolean> pushBlocksConfig = register(new BooleanConfig("NoPush-Blocks", "Prevents being pushed out of blocks", true));
    Config<Boolean> pushLiquidsConfig = register(new BooleanConfig("NoPush-Liquids", "Prevents being pushed by flowing liquids", true));
    Config<Boolean> pushFishhookConfig = register(new BooleanConfig("NoPush-Fishhook", "Prevents being pulled by fishing rod hooks", true));
    //
    private boolean cancelVelocity;

    /**
     *
     */
    public VelocityModule()
    {
        super("Velocity", "Reduces the amount of player knockback velocity", ModuleCategory.MOVEMENT);
        INSTANCE = this;
    }

    public static VelocityModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        if (modeConfig.getValue() == VelocityMode.NORMAL)
        {
            DecimalFormat decimal = new DecimalFormat("0.0");
            return String.format("H:%s%%, V:%s%%",
                    decimal.format(horizontalConfig.getValue()),
                    decimal.format(verticalConfig.getValue()));
        }
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onEnable()
    {
        cancelVelocity = false;
    }

    @Override
    public void onDisable()
    {
        if (cancelVelocity)
        {
            if (modeConfig.getValue() == VelocityMode.GRIM)
            {
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();
                if (Managers.ROTATION.isRotating())
                {
                    yaw = Managers.ROTATION.getRotationYaw();
                    pitch = Managers.ROTATION.getRotationPitch();
                }
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                        mc.player.getBlockPos(), Direction.DOWN));
            }
            cancelVelocity = false;
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && knockbackConfig.getValue())
        {
            if (packet.getId() != mc.player.getId())
            {
                return;
            }
            switch (modeConfig.getValue())
            {
                case NORMAL ->
                {
                    if (horizontalConfig.getValue() == 0.0f && verticalConfig.getValue() == 0.0f)
                    {
                        event.cancel();
                        return;
                    }
                    ((AccessorEntityVelocityUpdateS2CPacket) packet).setVelocityX((int) (packet.getVelocityX()
                            * (horizontalConfig.getValue() / 100.0f)));
                    ((AccessorEntityVelocityUpdateS2CPacket) packet).setVelocityY((int) (packet.getVelocityY()
                            * (verticalConfig.getValue() / 100.0f)));
                    ((AccessorEntityVelocityUpdateS2CPacket) packet).setVelocityZ((int) (packet.getVelocityZ()
                            * (horizontalConfig.getValue() / 100.0f)));
                }
                case GRIM ->
                {
                    if (!Managers.ANTICHEAT.hasPassed(100))
                    {
                        return;
                    }
                    event.cancel();
                    cancelVelocity = true;
                }
                case GRIM_V3 ->
                {
                    if (isPhased())
                    {
                        event.cancel();
                    }
                }
            }
        }
        else if (event.getPacket() instanceof ExplosionS2CPacket packet && explosionConfig.getValue())
        {
            switch (modeConfig.getValue())
            {
                case NORMAL ->
                {
                    if (horizontalConfig.getValue() == 0.0f && verticalConfig.getValue() == 0.0f)
                    {
                        event.cancel();
                    }
                    else
                    {
                        ((AccessorExplosionS2CPacket) packet).setPlayerVelocityX(packet.getPlayerVelocityX()
                                * (horizontalConfig.getValue() / 100.0f));
                        ((AccessorExplosionS2CPacket) packet).setPlayerVelocityY(packet.getPlayerVelocityY()
                                * (verticalConfig.getValue() / 100.0f));
                        ((AccessorExplosionS2CPacket) packet).setPlayerVelocityZ(packet.getPlayerVelocityZ()
                                * (horizontalConfig.getValue() / 100.0f));
                    }
                }
                case GRIM ->
                {
                    if (!Managers.ANTICHEAT.hasPassed(100))
                    {
                        return;
                    }
                    event.cancel();
                    cancelVelocity = true;
                }
                case GRIM_V3 ->
                {
                    if (isPhased())
                    {
                        event.cancel();
                    }
                }
            }
            if (event.isCanceled())
            {
                // Dumb fix bc canceling explosion velocity removes explosion handling in 1.19
                mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet.getX(), packet.getY(), packet.getZ(),
                        SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
                        4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));
            }
        }
        else if (event.getPacket() instanceof BundleS2CPacket packet && modeConfig.getValue() == VelocityMode.GRIM_V3 && isPhased())
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof ExplosionS2CPacket packet2)
                {
                    event.cancel();
                    mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet2.getX(), packet2.getY(), packet2.getZ(),
                            SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
                            4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));
                    break;
                }
                else if (packet1 instanceof EntityVelocityUpdateS2CPacket)
                {
                    event.cancel();
                    break;
                }
            }
        }
        else if (event.getPacket() instanceof EntityDamageS2CPacket packet && packet.entityId() == mc.player.getId() && modeConfig.getValue() == VelocityMode.GRIM_V3 && isPhased())
        {
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            // Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
        else if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getStatus() == EntityStatuses.PULL_HOOKED_ENTITY && pushFishhookConfig.getValue())
        {
            Entity entity = packet.getEntity(mc.world);
            if (entity instanceof FishingBobberEntity hook && hook.getHookedEntity() == mc.player)
            {
                event.cancel();
            }
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (cancelVelocity)
        {
            if (modeConfig.getValue() == VelocityMode.GRIM)
            {
                // Fixes issue with rotations
                float yaw = Managers.ROTATION.getServerYaw();
                float pitch = Managers.ROTATION.getServerPitch();
                if (Managers.ROTATION.isRotating())
                {
                    yaw = Managers.ROTATION.getRotationYaw();
                    pitch = Managers.ROTATION.getRotationPitch();
                }
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                        mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
            }
            cancelVelocity = false;
        }
    }

    @EventListener
    public void onPushEntity(PushEntityEvent event)
    {
        if (pushEntitiesConfig.getValue() && event.getPushed().equals(mc.player))
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event)
    {
        if (pushBlocksConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPushFluid(PushFluidsEvent event)
    {
        if (pushLiquidsConfig.getValue())
        {
            event.cancel();
        }
    }

    private boolean isPhased()
    {
        for (BlockPos blockPos : PositionUtil.getAllInBox(mc.player.getBoundingBox()))
        {
            if (!mc.world.getBlockState(blockPos).isReplaceable())
            {
                return true;
            }
        }
        return false;
    }

    private enum VelocityMode
    {
        NORMAL,
        GRIM,
        GRIM_V3
    }
}
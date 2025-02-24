package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.player.PushEntityEvent;
import net.shoreline.client.impl.event.entity.player.PushFluidsEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.network.PushOutOfBlocksEvent;
import net.shoreline.client.impl.module.combat.SurroundModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorBundlePacket;
import net.shoreline.client.mixin.accessor.AccessorClientWorld;
import net.shoreline.client.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket;
import net.shoreline.client.mixin.accessor.AccessorExplosionS2CPacket;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
    Config<Float> horizontalConfig = register(new NumberConfig<>("Horizontal", "How much horizontal knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> modeConfig.getValue() == VelocityMode.NORMAL || modeConfig.getValue() == VelocityMode.WALLS));
    Config<Float> verticalConfig = register(new NumberConfig<>("Vertical", "How much vertical knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> modeConfig.getValue() == VelocityMode.NORMAL || modeConfig.getValue() == VelocityMode.WALLS));
    Config<Boolean> concealConfig = register(new BooleanConfig("Conceal", "Fixes velocity on servers with excessive setbacks", false));
    Config<Boolean> wallsAirConfig = register(new BooleanConfig("GroundOnly", "Only applies velocity in walls while on ground", false, () -> modeConfig.getValue() == VelocityMode.WALLS));
    Config<Boolean> wallsTrappedConfig = register(new BooleanConfig("Trapped", "Applies velocity while player head is trapped", false, () -> modeConfig.getValue() == VelocityMode.WALLS));
    Config<Boolean> pushEntitiesConfig = register(new BooleanConfig("NoPush-Entities", "Prevents being pushed away from entities", true));
    Config<Boolean> pushBlocksConfig = register(new BooleanConfig("NoPush-Blocks", "Prevents being pushed out of blocks", true));
    Config<Boolean> pushLiquidsConfig = register(new BooleanConfig("NoPush-Liquids", "Prevents being pushed by flowing liquids", true));
    Config<Boolean> pushFishhookConfig = register(new BooleanConfig("NoPush-Fishhook", "Prevents being pulled by fishing rod hooks", true));
    //
    private boolean cancelVelocity;
    private boolean concealVelocity;

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
                        mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                if (Managers.NETWORK.isCrystalPvpCC())
//                {
//                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
//                            mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                }
            }
            cancelVelocity = false;
        }

        concealVelocity = false;
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && concealConfig.getValue())
        {
            concealVelocity = true;
        }

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && knockbackConfig.getValue())
        {
            if (packet.getEntityId() != mc.player.getId())
            {
                return;
            }

            if (concealVelocity && packet.getVelocityX() == 0 && packet.getVelocityZ() == 0 && packet.getVelocityZ() == 0)
            {
                concealVelocity = false;
                return;
            }

            if (modeConfig.getValue() == VelocityMode.WALLS)
            {
                if (!isPhased() && (!wallsTrappedConfig.getValue() || !isWallsTrapped()))
                {
                    return;
                }

                if (wallsAirConfig.getValue() && !Managers.POSITION.isOnGround())
                {
                    return;
                }
            }

            switch (modeConfig.getValue())
            {
                case NORMAL, WALLS ->
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

                case GRIM_V3 -> event.setCanceled(isPhased());
            }
        }
        else if (event.getPacket() instanceof ExplosionS2CPacket packet && explosionConfig.getValue())
        {
            if (modeConfig.getValue() == VelocityMode.WALLS && !isPhased())
            {
                return;
            }

            switch (modeConfig.getValue())
            {
                case NORMAL, WALLS ->
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

                case GRIM_V3 -> event.setCanceled(isPhased());
            }

            if (event.isCanceled())
            {
                // Dumb fix bc canceling explosion velocity removes explosion handling in 1.19
                mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet.getX(), packet.getY(), packet.getZ(),
                        SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS,
                        4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));
            }
        }

        else if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            List<Packet<?>> allowedBundle = new ArrayList<>();

            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof ExplosionS2CPacket packet2 && explosionConfig.getValue())
                {
                    mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet2.getX(), packet2.getY(), packet2.getZ(),
                            SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS,
                            4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));

                    if (modeConfig.getValue() == VelocityMode.WALLS && !isPhased())
                    {
                        allowedBundle.add(packet1);
                        continue;
                    }

                    switch (modeConfig.getValue())
                    {
                        case NORMAL, WALLS ->
                        {
                            if (horizontalConfig.getValue() == 0.0f && verticalConfig.getValue() == 0.0f)
                            {
                                continue;
                            }
                            else
                            {
                                ((AccessorExplosionS2CPacket) packet2).setPlayerVelocityX(packet2.getPlayerVelocityX()
                                        * (horizontalConfig.getValue() / 100.0f));
                                ((AccessorExplosionS2CPacket) packet2).setPlayerVelocityY(packet2.getPlayerVelocityY()
                                        * (verticalConfig.getValue() / 100.0f));
                                ((AccessorExplosionS2CPacket) packet2).setPlayerVelocityZ(packet2.getPlayerVelocityZ()
                                        * (horizontalConfig.getValue() / 100.0f));
                            }
                        }
                        case GRIM ->
                        {
                            if (Managers.ANTICHEAT.hasPassed(100))
                            {
                                allowedBundle.add(packet1);
                                continue;
                            }

                            cancelVelocity = true;
                            continue;
                        }
                        case GRIM_V3 ->
                        {
                            if (isPhased())
                            {
                                continue;
                            }
                        }
                    }
                }
                else if (packet1 instanceof EntityVelocityUpdateS2CPacket packet2 && knockbackConfig.getValue())
                {
                    if (packet2.getEntityId() != mc.player.getId())
                    {
                        allowedBundle.add(packet1);
                        continue;
                    }

                    if (modeConfig.getValue() == VelocityMode.WALLS)
                    {
                        if (!isPhased() && (!wallsTrappedConfig.getValue() || !isWallsTrapped()))
                        {
                            allowedBundle.add(packet1);
                            return;
                        }

                        if (wallsAirConfig.getValue() && !Managers.POSITION.isOnGround())
                        {
                            allowedBundle.add(packet1);
                            continue;
                        }
                    }

                    switch (modeConfig.getValue())
                    {
                        case NORMAL, WALLS ->
                        {
                            if (horizontalConfig.getValue() == 0.0f && verticalConfig.getValue() == 0.0f)
                            {
                                continue;
                            }
                            else
                            {
                                ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocityX((int) (packet2.getVelocityX()
                                        * (horizontalConfig.getValue() / 100.0f)));
                                ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocityY((int) (packet2.getVelocityY()
                                        * (verticalConfig.getValue() / 100.0f)));
                                ((AccessorEntityVelocityUpdateS2CPacket) packet2).setVelocityZ((int) (packet2.getVelocityZ()
                                        * (horizontalConfig.getValue() / 100.0f)));
                            }
                        }
                        case GRIM ->
                        {
                            if (!Managers.ANTICHEAT.hasPassed(100))
                            {
                                allowedBundle.add(packet1);
                                continue;
                            }

                            cancelVelocity = true;
                            continue;
                        }
                        case GRIM_V3 ->
                        {
                            if (isPhased())
                            {
                                continue;
                            }
                        }
                    }
                }

                allowedBundle.add(packet1);
            }

            ((AccessorBundlePacket) packet).setIterable(allowedBundle);
        }

        else if (event.getPacket() instanceof EntityDamageS2CPacket packet
                && packet.entityId() == mc.player.getId()
                && modeConfig.getValue() == VelocityMode.GRIM_V3 && isPhased())
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
        concealVelocity = false;

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
//                if (Managers.NETWORK.isCrystalPvpCC())
//                {
//                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
//                            mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
//                }
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

    private boolean isWallsTrapped()
    {
        BlockPos headPos = mc.player.getBlockPos().up(mc.player.isCrawling() ? 1 : 2);
        if (mc.world.getBlockState(headPos).isReplaceable())
        {
            return false;
        }

        return SurroundModule.getInstance().getSurroundNoDown(mc.player).stream()
                .noneMatch(blockPos -> mc.world.getBlockState(mc.player.isCrawling() ? blockPos : blockPos.up()).isReplaceable());
    }

    private boolean isPhased()
    {
        return PositionUtil.getAllInBox(mc.player.getBoundingBox()).stream()
                .anyMatch(blockPos -> !mc.world.getBlockState(blockPos).isReplaceable());
    }

    private enum VelocityMode
    {
        NORMAL,
        WALLS,
        GRIM,
        GRIM_V3
    }
}
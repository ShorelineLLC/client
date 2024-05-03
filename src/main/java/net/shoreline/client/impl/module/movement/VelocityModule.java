package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.player.PushEntityEvent;
import net.shoreline.client.impl.event.entity.player.PushFluidsEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PushOutOfBlocksEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorClientWorld;
import net.shoreline.client.mixin.accessor.AccessorEntityVelocityUpdateS2CPacket;
import net.shoreline.client.mixin.accessor.AccessorExplosionS2CPacket;
import net.shoreline.client.util.string.EnumFormatter;

import java.text.DecimalFormat;

/**
 * @author Gavin, linus
 * @since 1.0
 */
public class VelocityModule extends ToggleModule {
    Config<Boolean> knockbackConfig = new BooleanConfig("Knockback", "Removes player knockback velocity", true);
    Config<Boolean> explosionConfig = new BooleanConfig("Explosion", "Removes player explosion velocity", true);
    Config<VelocityMode> modeConfig = new EnumConfig<>("Mode", "The mode for velocity", VelocityMode.NORMAL, VelocityMode.values());
    Config<Float> horizontalConfig = new NumberConfig<>("Horizontal", "How much horizontal knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> modeConfig.getValue() == VelocityMode.NORMAL);
    Config<Float> verticalConfig = new NumberConfig<>("Vertical", "How much vertical knock-back to take", 0.0f, 0.0f, 100.0f, NumberDisplay.PERCENT, () -> modeConfig.getValue() == VelocityMode.NORMAL);
    Config<Boolean> pushEntitiesConfig = new BooleanConfig("NoPush-Entities", "Prevents being pushed away from entities", true);
    Config<Boolean> pushBlocksConfig = new BooleanConfig("NoPush-Blocks", "Prevents being pushed out of blocks", true);
    Config<Boolean> pushLiquidsConfig = new BooleanConfig("NoPush-Liquids", "Prevents being pushed by flowing liquids", true);
    Config<Boolean> pushFishhookConfig = new BooleanConfig("NoPush-Fishhook", "Prevents being pulled by fishing rod hooks", true);
    //
    private boolean cancelVelocity;

    /**
     *
     */
    public VelocityModule() {
        super("Velocity", "Reduces the amount of player knockback velocity",
                ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData() {
        if (modeConfig.getValue() == VelocityMode.NORMAL) {
            DecimalFormat decimal = new DecimalFormat("0.0");
            return String.format("H:%s%%, V:%s%%",
                    decimal.format(horizontalConfig.getValue()),
                    decimal.format(verticalConfig.getValue()));
        }
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onEnable() {
        cancelVelocity = false;
    }

    @Override
    public void onDisable() {
        if (cancelVelocity) {
            if (modeConfig.getValue() == VelocityMode.GRIM) {
                float yaw = Managers.ROTATION.getServerYaw();
                float pitch = Managers.ROTATION.getServerPitch();
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround()));
                if (!mc.player.isCrawling()) {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                            mc.player.getBlockPos().up(), Direction.DOWN));
                }
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                        mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
            }
            cancelVelocity = false;
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && knockbackConfig.getValue()) {
            if (packet.getId() != mc.player.getId()) {
                return;
            }
            switch (modeConfig.getValue()) {
                case NORMAL -> {
                    if (horizontalConfig.getValue() == 0.0f && verticalConfig.getValue() == 0.0f) {
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
                case GRIM -> {
                    if (!Managers.ANTICHEAT.hasPassed(100)) {
                        return;
                    }
                    event.cancel();
                    cancelVelocity = true;
                }
            }
        } else if (event.getPacket() instanceof ExplosionS2CPacket packet && explosionConfig.getValue()) {
            switch (modeConfig.getValue()) {
                case NORMAL -> {
                    if (horizontalConfig.getValue() == 0.0f && verticalConfig.getValue() == 0.0f) {
                        event.cancel();
                    } else {
                        ((AccessorExplosionS2CPacket) packet).setPlayerVelocityX(packet.getPlayerVelocityX()
                                * (horizontalConfig.getValue() / 100.0f));
                        ((AccessorExplosionS2CPacket) packet).setPlayerVelocityY(packet.getPlayerVelocityY()
                                * (verticalConfig.getValue() / 100.0f));
                        ((AccessorExplosionS2CPacket) packet).setPlayerVelocityZ(packet.getPlayerVelocityZ()
                                * (horizontalConfig.getValue() / 100.0f));
                    }
                }
                case GRIM -> {
                    if (!Managers.ANTICHEAT.hasPassed(100)) {
                        return;
                    }
                    event.cancel();
                    cancelVelocity = true;
                }
            }
            if (event.isCanceled()) {
                // Dumb fix bc canceling explosion velocity removes explosion handling in 1.19
                mc.executeSync(() -> ((AccessorClientWorld) mc.world).hookPlaySound(packet.getX(), packet.getY(), packet.getZ(),
                        SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS,
                        4.0f, (1.0f + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f) * 0.7f, false, RANDOM.nextLong()));
            }
        } else if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getStatus() == EntityStatuses.PULL_HOOKED_ENTITY && pushFishhookConfig.getValue()) {
            Entity entity = packet.getEntity(mc.world);
            if (entity instanceof FishingBobberEntity hook && hook.getHookedEntity() == mc.player) {
                event.cancel();
            }
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && cancelVelocity) {
            if (modeConfig.getValue() == VelocityMode.GRIM && Managers.ANTICHEAT.hasPassed(100)) {
                // Fixes issue with rotations
                float yaw = Managers.ROTATION.getServerYaw();
                float pitch = Managers.ROTATION.getServerPitch();
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ(), yaw, pitch, mc.player.isOnGround()));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                        mc.player.isCrawling() ? mc.player.getBlockPos() : mc.player.getBlockPos().up(), Direction.DOWN));
            }
            cancelVelocity = false;
        }
    }

    @EventListener
    public void onPushEntity(PushEntityEvent event) {
        if (pushEntitiesConfig.getValue() && event.getPushed().equals(mc.player)) {
            event.cancel();
        }
    }

    @EventListener
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
        if (pushBlocksConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onPushFluid(PushFluidsEvent event) {
        if (pushLiquidsConfig.getValue()) {
            event.cancel();
        }
    }

    private enum VelocityMode {
        NORMAL,
        GRIM
    }
}
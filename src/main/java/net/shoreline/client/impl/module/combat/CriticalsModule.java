package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2SPacket;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.network.InteractType;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.client.util.world.EntityUtil;

/**
 * @author linus
 * @since 1.0
 */
public class CriticalsModule extends ToggleModule {
    private static CriticalsModule INSTANCE;

    //
    Config<CritMode> modeConfig = register(new EnumConfig<>("Mode", "Mode for critical attack modifier", CritMode.PACKET, CritMode.values()));
    //
    private final Timer attackTimer = new CacheTimer();

    /**
     *
     */
    public CriticalsModule() {
        super("Criticals", "Modifies attacks to always land critical hits", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static CriticalsModule getInstance() {
        return INSTANCE;
    }

    /**
     * @return
     */
    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    /**
     * @param event
     */
    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        // Custom aura crit handling
        if (mc.player == null || mc.world == null || AuraModule.getInstance().isEnabled()) {
            return;
        }
        if (event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet
                && packet.getType() == InteractType.ATTACK) {
            if (mc.player.isRiding()
                    || mc.player.isFallFlying()
                    || mc.player.isTouchingWater()
                    || mc.player.isInLava()
                    || mc.player.isHoldingOntoLadder()
                    || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                    || InventoryUtil.isHolding32k()) {
                return;
            }
            // Attacked entity
            final Entity e = packet.getEntity();
            if (e == null || !e.isAlive() || e instanceof EndCrystalEntity) {
                return;
            }
            if (EntityUtil.isVehicle(e)) {
                if (modeConfig.getValue() == CritMode.PACKET) {
                    for (int i = 0; i < 5; ++i) {
                        Managers.NETWORK.sendQuietPacket(PlayerInteractEntityC2SPacket.attack(e,
                                Managers.POSITION.isSneaking()));
                        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                }
                return;
            }
            preAttackPacket(e);
        }
    }

    /**
     * Callback method for pre attack stage, must be called before the attack
     * packet or else the movements will not be registered
     *
     * @see AuraModule#postAttackTarget(Entity)
     */
    public void preAttackPacket(Entity e) {
        double x = Managers.POSITION.getX();
        double y = Managers.POSITION.getY();
        double z = Managers.POSITION.getZ();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        if (Managers.ROTATION.isRotating())
        {
            yaw = Managers.ROTATION.getRotationYaw();
            pitch = Managers.ROTATION.getRotationPitch();
        }
        switch (modeConfig.getValue()) {
            case VANILLA -> {
                if (mc.player.isOnGround() && !mc.player.input.jumping) {
                    double d = 1.0e-7 + 1.0e-7 * (1.0 + RANDOM.nextInt(RANDOM.nextBoolean() ? 34 : 43));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.1016f + d * 3.0f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.0202f + d * 2.0f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 3.239e-4 + d, z, false));
                    mc.player.addCritParticles(e);
                }
            }
            case PACKET -> {
                if (mc.player.isOnGround() && !mc.player.input.jumping) {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.05f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.03f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y, z, false));
                    mc.player.addCritParticles(e);
                }
            }
            case PACKET_STRICT -> {
                if (attackTimer.passed(500) && mc.player.isOnGround() && !mc.player.input.jumping) {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.11f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.1100013579f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.0000013579f, z, false));
                    attackTimer.reset();
                    mc.player.addCritParticles(e);
                }
            }
            case GRIM -> {
                if (!mc.player.isOnGround()) {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y - 0.000001, z, yaw, pitch, false));
                }
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.00150000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.014400000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.001150000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
            }
            case LOW_HOP -> {
                // mc.player.jump();
                Managers.MOVEMENT.setMotionY(0.3425);
            }
        }
    }

    public boolean isGrim() {
        return modeConfig.getValue() == CritMode.GRIM;
    }

    public enum CritMode {
        PACKET,
        PACKET_STRICT,
        VANILLA,
        GRIM,
        LOW_HOP
    }
}

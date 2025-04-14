package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2SPacket;
import net.shoreline.client.impl.module.world.AutoMineModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.math.position.PositionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.network.InteractType;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class CriticalsModule extends ToggleModule
{
    private static CriticalsModule INSTANCE;

    //
    Config<Boolean> multitaskConfig = register(new BooleanConfig("Multitask", "Allows crits when other combat modules are enabled", true));
    Config<CritMode> modeConfig = register(new EnumConfig<>("Mode", "Mode for critical attack modifier", CritMode.PACKET, CritMode.values()));
    Config<Boolean> phaseOnlyConfig = register(new BooleanConfig("PhasedOnly", "Only attempts criticals when phased", false, () -> modeConfig.getValue() == CritMode.GRIM_V3 || modeConfig.getValue() == CritMode.GRIM));
    Config<Boolean> wallsOnlyConfig = register(new BooleanConfig("WallsOnly", "Only attempts criticals in walls", false, () -> (modeConfig.getValue() == CritMode.GRIM_V3 || modeConfig.getValue() == CritMode.GRIM) && phaseOnlyConfig.getValue()));
    Config<Boolean> moveFixConfig = register(new BooleanConfig("MoveFix", "Pauses crits when moving", false, () -> modeConfig.getValue() == CritMode.GRIM_V3 || modeConfig.getValue() == CritMode.GRIM));
    //
    private final Timer attackTimer = new CacheTimer();
    private boolean postUpdateGround;
    private boolean postUpdateSprint;

    /**
     *
     */
    public CriticalsModule()
    {
        super("Criticals", "Modifies attacks to always land critical hits", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static CriticalsModule getInstance()
    {
        return INSTANCE;
    }

    /**
     * @return
     */
    @Override
    public String getModuleData()
    {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @Override
    public void onDisable()
    {
        postUpdateGround = false;
        postUpdateSprint = false;
    }

    /**
     * @param event
     */
    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        // Custom aura crit handling
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (AutoCrystalModule.getInstance().isAttacking() || AutoCrystalModule.getInstance().isPlacing())
        {
            return;
        }

        // All combat modules have priority
        if (!multitaskConfig.getValue() && (SurroundModule.getInstance().isPlacing()
                || SelfTrapModule.getInstance().isPlacing()
                || AutoTrapModule.getInstance().isPlacing()
                || AutoCrawlTrapModule.getInstance().isPlacing()
                || AutoWebModule.getInstance().isPlacing()
                || HoleFillModule.getInstance().isPlacing()
                || AutoXPModule.getInstance().isEnabled()
                || AutoMineModule.getInstance().isEnabled()))
        {
            return;
        }

        if (event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet
                && packet.getType() == InteractType.ATTACK)
        {
            if (mc.player.isRiding() || mc.player.isFallFlying()
                    || mc.player.isTouchingWater()
                    || mc.player.isInLava()
                    || mc.player.isHoldingOntoLadder()
                    || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                    || InventoryUtil.isHolding32k())
            {
                return;
            }

            // Attacked entity
            final Entity e = packet.getEntity();
            if (e == null || !e.isAlive() || !(e instanceof LivingEntity))
            {
                return;
            }
            if (EntityUtil.isVehicle(e))
            {
                if (modeConfig.getValue() == CritMode.PACKET)
                {
                    for (int i = 0; i < 5; ++i)
                    {
                        Managers.NETWORK.sendQuietPacket(PlayerInteractEntityC2SPacket.attack(e,
                                Managers.POSITION.isSneaking()));
                        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                }
                return;
            }

            postUpdateSprint = mc.player.isSprinting();
            if (postUpdateSprint)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }

            attackSpoofJump(e);
        }
    }


    public void attackSpoofJump(Entity e)
    {
        double x = Managers.POSITION.getX();
        double y = Managers.POSITION.getY();
        double z = Managers.POSITION.getZ();
        switch (modeConfig.getValue())
        {
            case VANILLA ->
            {
                if (mc.player.isOnGround() && !mc.player.input.jumping)
                {
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
            case PACKET ->
            {
                if (mc.player.isOnGround() && !mc.player.input.jumping)
                {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 0.0625f, z, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y, z, false));
                    mc.player.addCritParticles(e);
                }
            }
            case PACKET_STRICT ->
            {
                if (attackTimer.passed(500) && mc.player.isOnGround() && !mc.player.input.jumping)
                {
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 1.1e-7f, z,false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y + 1.0e-8f, z, false));
                    postUpdateGround = true;
                    attackTimer.reset();
                }
            }
            case GRIM ->
            {
                if (phaseOnlyConfig.getValue() && (wallsOnlyConfig.getValue() ? !isDoublePhased() : !isPhased()))
                {
                    return;
                }

                if (moveFixConfig.getValue() && MovementUtil.isMovingInput())
                {
                    return;
                }

                if (attackTimer.passed(250) && mc.player.isOnGround() && !mc.player.isCrawling())
                {
                    float yaw = Managers.ROTATION.getServerYaw();
                    float pitch = Managers.ROTATION.getServerPitch();
                    if (Managers.ROTATION.isRotating())
                    {
                        yaw = Managers.ROTATION.getRotationYaw();
                        pitch = Managers.ROTATION.getRotationPitch();
                    }
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y + 0.0625, z, yaw, pitch, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y + 0.0625013579, z, yaw, pitch, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y + 1.3579e-6, z, yaw, pitch, false));
                    attackTimer.reset();
                }
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.00150000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.014400000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
//                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
//                        x, y + 0.001150000001304f, z, mc.player.getYaw(), mc.player.getPitch(), false));
            }
            case GRIM_V3 ->
            {
                if (phaseOnlyConfig.getValue() && (wallsOnlyConfig.getValue() ? !isDoublePhased() : !isPhased()))
                {
                    return;
                }

                if (moveFixConfig.getValue() && MovementUtil.isMovingInput())
                {
                    return;
                }

                if (mc.player.isOnGround() && !mc.player.isCrawling())
                {
//                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
//                            x, y + 0.00001058293536f, z, false));
//                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
//                            x, y + 0.00000916580235f, z, false));
//                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
//                            x, y + 0.00000010371854f, z, false));
                    float yaw = Managers.ROTATION.getServerYaw();
                    float pitch = Managers.ROTATION.getServerPitch();
                    if (Managers.ROTATION.isRotating())
                    {
                        yaw = Managers.ROTATION.getRotationYaw();
                        pitch = Managers.ROTATION.getRotationPitch();
                    }
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y, z, yaw, pitch, true));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y + 0.0625f, z, yaw, pitch, false));
                    Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(
                            x, y + 0.04535f, z, yaw, pitch, false));
                }
            }
            case LOW_HOP ->
            {
                // mc.player.jump();
                Managers.MOVEMENT.setMotionY(0.3425);
            }
        }
    }

    @EventListener
    public void onPacketOutboundPost(PacketEvent.OutboundPost event)
    {
        if (mc.player == null)
        {
            return;
        }

        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket)
        {
            if (postUpdateGround)
            {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
                postUpdateGround = false;
            }

            if (postUpdateSprint)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                postUpdateSprint = false;
            }
        }
    }

    public boolean isGrim()
    {
        return modeConfig.getValue() == CritMode.GRIM;
    }

    public boolean isDoublePhased()
    {
        for (BlockPos pos : PositionUtil.getAllInBox(mc.player.getBoundingBox(), mc.player.getBlockPos()))
        {
            BlockState state = mc.world.getBlockState(pos);
            BlockState state2 = mc.world.getBlockState(pos.up());
            if (state.blocksMovement() && state2.blocksMovement())
            {
                return true;
            }
        }
        return false;
    }

    public boolean isPhased()
    {
        for (BlockPos pos : PositionUtil.getAllInBox(mc.player.getBoundingBox()))
        {
            if (mc.world.getBlockState(pos).blocksMovement())
            {
                return true;
            }
        }

        return false;
    }

    public enum CritMode
    {
        PACKET,
        PACKET_STRICT,
        VANILLA,
        GRIM,
        GRIM_V3,
        LOW_HOP
    }
}

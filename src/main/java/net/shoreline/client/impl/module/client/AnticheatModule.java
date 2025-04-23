package net.shoreline.client.impl.module.client;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;
import net.shoreline.client.util.math.position.DirectionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

public class AnticheatModule extends ConcurrentModule
{
    private static AnticheatModule INSTANCE;

    Config<Anticheats> modeConfig = register(new EnumConfig<>("Mode", "Applies anticheat optimizations", Anticheats.VANILLA, Anticheats.values()));
    Config<Boolean> miningFixConfig = register(new BooleanConfig("MiningFix", "Fixes vanilla mining on GrimV3", false, () -> modeConfig.getValue() == Anticheats.GRIM));
    Config<Boolean> webJumpFixConfig = register(new BooleanConfig("WebJumpFix", "Fixes sprint jumping in webs on grim", false, () -> modeConfig.getValue() == Anticheats.GRIM));
    Config<Boolean> raytraceSpoofConfig = register(new BooleanConfig("RaytraceFix", "Allows you to spoof your raytrace", false, () -> modeConfig.getValue() == Anticheats.N_C_P));

    private final Timer raytraceTimer = new CacheTimer();
    private float pitch = Float.NaN;

    public AnticheatModule()
    {
        super("Anticheat", "Settings for anticheat configs", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static AnticheatModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener(priority = 10000)
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }

        if (isNCP() && raytraceSpoofConfig.getValue() && event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && raytraceTimer.passed(250))
        {
            BlockHitResult packetResult = packet.getBlockHitResult();
            BlockPos pos = packetResult.getBlockPos();
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    mc.player.getEyePos(), DirectionUtil.getDirectionOffsetPos(pos, packetResult.getSide()),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, mc.player));
            if (mc.world.isSpaceEmpty(mc.player.getBoundingBox().stretch(0.0, 0.15, 0.0)) && result != null
                    && result.getType() == HitResult.Type.BLOCK && !result.getBlockPos().equals(pos))
            {
                pitch = -75;
                raytraceTimer.reset();
            }
        }

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook() && !Float.isNaN(pitch))
        {
            ((AccessorPlayerMoveC2SPacket) packet).hookSetPitch(pitch);
            pitch = Float.NaN;
        }

        if (isGrim() && miningFixConfig.getValue() && event.getPacket() instanceof PlayerActionC2SPacket packet
                && (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                || packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                || packet.getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK))
        {
            if (BlastResistantBlocks.isUnbreakable(packet.getPos()))
            {
                event.cancel();
                return;
            }

            if (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK)
            {
                Managers.NETWORK.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                Managers.NETWORK.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                Managers.NETWORK.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                event.cancel();
            }
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.POST &&
                event.getConfig() == raytraceSpoofConfig && !raytraceSpoofConfig.getValue())
        {
            pitch = Float.NaN;
        }
    }

    public boolean isGrim()
    {
        return modeConfig.getValue() == Anticheats.GRIM;
    }

    public boolean isNCP()
    {
        return modeConfig.getValue() == Anticheats.N_C_P;
    }

    public boolean getMiningFix()
    {
        return miningFixConfig.getValue();
    }

    public boolean getWebJumpFix()
    {
        return webJumpFixConfig.getValue();
    }

    private enum Anticheats
    {
        GRIM,
        N_C_P,
        VANILLA
    }
}

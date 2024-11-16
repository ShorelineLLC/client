package net.shoreline.client.impl.module.client;

import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;
import net.shoreline.client.util.math.position.DirectionUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class RotationsModule extends ConcurrentModule
{
    private static RotationsModule INSTANCE;

    //
    Config<Float> preserveTicksConfig = register(new NumberConfig<>("PreserveTicks", "Time to preserve rotations after reaching the target rotations", 0.0f, 10.0f, 20.0f));
    Config<Boolean> movementFixConfig = register(new BooleanConfig("MovementFix", "Fixes movement on Grim when rotating", false));
    Config<Boolean> mouseSensFixConfig = register(new BooleanConfig("MouseSensFix", "Fixes movement on Grim when applying mouse sensitivity", false));
    Config<Boolean> raytraceSpoofConfig = register(new BooleanConfig("RaytraceSpoof", "Allows you to spoof your raytrace", false));
    //
    private float prevYaw;

    private final Timer raytraceTimer = new CacheTimer();
    private float pitch = Float.NaN;

    /**
     *
     */
    public RotationsModule()
    {
        super("Rotations", "Manages client rotations", ModuleCategory.CLIENT);
        INSTANCE = this;
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

    @EventListener(priority = 10000)
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null || mc.world == null || !raytraceSpoofConfig.getValue())
        {
            return;
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && raytraceTimer.passed(250))
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
    }

    public static RotationsModule getInstance()
    {
        return INSTANCE;
    }

    public boolean getMovementFix()
    {
        return movementFixConfig.getValue();
    }

    public boolean getMouseSensFix()
    {
        return mouseSensFixConfig.getValue();
    }

    /**
     * @return
     */
    public float getPreserveTicks()
    {
        return preserveTicksConfig.getValue();
    }
}

package net.shoreline.client.impl.module.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.module.exploit.PacketFlyModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class NoFallModule extends ToggleModule
{

    //
    Config<NoFallMode> modeConfig = register(new EnumConfig<>("Mode", "The mode to prevent fall damage", NoFallMode.ANTI, NoFallMode.values()));

    private double groundedY;

    public NoFallModule()
    {
        super("NoFall", "Prevents all fall damage", ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData()
    {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE || !checkFalling())
        {
            return;
        }
        if (modeConfig.getValue() == NoFallMode.LIMIT)
        {
            if (mc.world.getRegistryKey() == World.NETHER)
            {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), 0, mc.player.getZ(), true));
            }
            else
            {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(0, 64, 0, true));
            }
            mc.player.fallDistance = 0.0f;
        }
        else if (modeConfig.getValue() == NoFallMode.GRIM)
        {
            Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 1.0e-9,
                    mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true));
            mc.player.onLanding();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null || !checkFalling())
        {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet)
        {
            if (modeConfig.getValue() == NoFallMode.PACKET)
            {
                ((AccessorPlayerMoveC2SPacket) packet).hookSetOnGround(true);
            }
            else if (modeConfig.getValue() == NoFallMode.ANTI)
            {
                double y = packet.getY(mc.player.getY());
                ((AccessorPlayerMoveC2SPacket) packet).hookSetY(y + 0.10000000149011612);
            }
            else if (modeConfig.getValue() == NoFallMode.STRICT)
            {
                if (isFallingLagback() && mc.player.fallDistance >= 3.0f)
                {
                    Managers.MOVEMENT.setMotionY(0.0);
                    ((AccessorPlayerMoveC2SPacket) packet).hookSetY(groundedY);
                    mc.player.fallDistance = 0.0f;
                }
            }
        }
    }

    public double getGroundY()
    {
        for (int i = (int) Math.round(mc.player.getY()); i > 0; --i)
        {
            Box bb = mc.player.getBoundingBox();
            Box box = new Box(bb.minX, i - 1.0, bb.minZ, bb.maxX, i, bb.maxZ);
            if (!mc.world.isSpaceEmpty(box) || box.minY > mc.player.getY())
            {
                continue;
            }
            return i;
        }
        return 0.0;
    }

    public boolean isFallingLagback()
    {
        groundedY = getGroundY() - 0.1;
        return mc.player.getY() - groundedY < 3.0;
    }

    private boolean checkFalling()
    {
        return mc.player.fallDistance > mc.player.getSafeFallDistance() && !mc.player.isOnGround()
                && !mc.player.isFallFlying() && !FlightModule.getInstance().isEnabled() && !PacketFlyModule.getInstance().isEnabled();
    }

    public enum NoFallMode
    {
        ANTI,
        LIMIT,
        PACKET,
        STRICT,
        GRIM
    }
}

package net.shoreline.client.impl.module.misc;

import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.gui.screen.MenuDisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;

import java.time.Instant;
import java.util.BitSet;

import static net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN;

/**
 * @author xgraza
 * @since 1.0
 */
public final class ServerModule extends ConcurrentModule
{
    Config<Boolean> demoConfig = register(new BooleanConfig("NoDemo", "Prevents servers from forcing you to a demo screen", true));
    Config<Boolean> resourcePackConfig = register(new BooleanConfig("NoResourcePack", "Prevents server from forcing resource pack", false));
    Config<Boolean> antiCrashConfig = register(new BooleanConfig("NoServerCrash", "Prevents server packets from crashing the client", false));
    Config<Boolean> illegalDisconnectConfig = register(new BooleanConfig("IllegalDisconnect", "Disconnects by getting kicked from server", false));

    public ServerModule()
    {
        super("Server", "Prevents servers actions on player", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof GameStateChangeS2CPacket packet)
        {
            if (packet.getReason() == DEMO_MESSAGE_SHOWN && !mc.isDemo() && demoConfig.getValue())
            {
                Shoreline.info("Server attempted to use Demo mode features on you!");
                event.cancel();
            }
        }
        if (event.getPacket() instanceof ResourcePackSendS2CPacket && resourcePackConfig.getValue())
        {
            event.cancel();
            Managers.NETWORK.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.DECLINED));
        }
        if (mc.world == null)
        {
            return;
        }
        if (antiCrashConfig.getValue())
        {
            // Out of bounds packets from server
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet
                    && (packet.getX() > 30000000 || packet.getY() > mc.world.getTopY()
                    || packet.getZ() > 30000000 || packet.getX() < -30000000
                    || packet.getY() < mc.world.getBottomY() || packet.getZ() < -30000000))
            {
                event.cancel();
            }
            else if (event.getPacket() instanceof ExplosionS2CPacket packet
                    && (packet.getX() > 30000000 || packet.getY() > mc.world.getTopY()
                    || packet.getZ() > 30000000 || packet.getX() < -30000000
                    || packet.getY() < mc.world.getBottomY() || packet.getZ() < -30000000
                    || packet.getRadius() > 1000 || packet.getAffectedBlocks().size() > 1000
                    || packet.getPlayerVelocityX() > 1000 || packet.getPlayerVelocityY() > 1000
                    || packet.getPlayerVelocityZ() > 1000 || packet.getPlayerVelocityX() < -1000
                    || packet.getPlayerVelocityY() < -1000 || packet.getPlayerVelocityZ() < -1000))
            {
                event.cancel();
            }
            else if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet
                    && (packet.getVelocityX() > 1000 || packet.getVelocityY() > 1000 ||
                    packet.getVelocityZ() > 1000 || packet.getVelocityX() < -1000 ||
                    packet.getVelocityY() < -1000 || packet.getVelocityZ() < -1000))
            {
                event.cancel();
            }
            else if (event.getPacket() instanceof ParticleS2CPacket packet && packet.getCount() > 500)
            {
                event.cancel();
            }
        }
    }

    @EventListener
    public void onMenuDisconnect(MenuDisconnectEvent event)
    {
        if (illegalDisconnectConfig.getValue())
        {
            // event.cancel();
            Managers.NETWORK.sendPacket(new ChatMessageC2SPacket(
                    "§",
                    Instant.now(),
                    NetworkEncryptionUtils.SecureRandomUtil.nextLong(),
                    null,
                    new LastSeenMessageList.Acknowledgment(1, new BitSet(2)))); // Illegal packet
        }
    }
}

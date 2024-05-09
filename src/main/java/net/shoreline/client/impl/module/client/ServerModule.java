package net.shoreline.client.impl.module.client;

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;

import static net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN;

/**
 * @author xgraza
 * @since 1.0
 */
public final class ServerModule extends ConcurrentModule
{
    Config<Boolean> packetKickConfig = new BooleanConfig("NoPacketKick", "Prevents thrown exceptions from kicking you", true);
    Config<Boolean> demoConfig = new BooleanConfig("NoDemo", "Prevents servers from forcing you to a demo screen", true);
    Config<Boolean> resourcePackConfig = new BooleanConfig("NoResourcePack", "Prevents server from forcing resource pack", false);

    public ServerModule()
    {
        super("Server", "Prevents servers actions on player", ModuleCategory.CLIENT);
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
        if (event.getPacket() instanceof ResourcePackSendS2CPacket && resourcePackConfig.getValue()) {
            event.cancel();
            Managers.NETWORK.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
        }
    }

    public boolean isPacketKick()
    {
        return packetKickConfig.getValue();
    }
}

package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class AutoAcceptModule extends ToggleModule
{
    //
    private final Timer acceptTimer = new CacheTimer();
    //
    Config<Float> delayConfig = register(new NumberConfig<>("Delay", "The delay before" +
            " accepting teleport requests", 0.0f, 3.0f, 10.0f));

    /**
     *
     */
    public AutoAcceptModule()
    {
        super("AutoAccept", "Automatically accepts teleport requests",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof ChatMessageS2CPacket packet)
        {
            String text = packet.body().content();
            if ((text.contains("has requested to teleport to you.")
                    || text.contains("has requested you teleport to them."))
                    && acceptTimer.passed(delayConfig.getValue() * 1000))
            {
                for (String friend : Managers.SOCIAL.getFriends())
                {
                    if (text.contains(friend))
                    {
                        ChatUtil.serverSendCommand("tpaccept");
                        break;
                    }
                }
            }
        }
    }
}

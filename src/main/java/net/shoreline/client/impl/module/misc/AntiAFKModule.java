package net.shoreline.client.impl.module.misc;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.config.setting.StringConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class AntiAFKModule extends RotationModule
{
    //
    Config<Float> secondsConfig = register(new NumberConfig<>("AFKTime", "The time before attempting actions", 5.0f, 20.0f, 120.0f));
    Config<Boolean> messageConfig = register(new BooleanConfig("Message", "Messages in chat to prevent AFK kick", true));
    Config<Boolean> tabCompleteConfig = register(new BooleanConfig("TabComplete", "Uses tab complete in chat to prevent AFK kick", true));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates the player to prevent AFK kick", true));
    Config<Boolean> jumpConfig = register(new BooleanConfig("Jump", "Jumps to prevent AFK kick", true));
    Config<Boolean> autoReplyConfig = register(new BooleanConfig("AutoReply", "Replies to players messaging you in chat", false));
    Config<String> replyConfig = register(new StringConfig("Reply", "The reply message for AutoReply", "I am currently AFK.", () -> autoReplyConfig.getValue()));
    Config<Float> delayConfig = register(new NumberConfig<>("Delay", "The delay between actions", 5.0f, 100.0f, 1000.0f));
    private final Timer afkTimer = new CacheTimer();
    private final Timer actionTimer = new CacheTimer();

    /**
     *
     */
    public AntiAFKModule()
    {
        super("AntiAFK", "Prevents the player from being kicked for AFK", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        afkTimer.reset();
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        afkTimer.reset();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        for (KeyBinding keyBinding : mc.options.allKeys)
        {
            if (keyBinding.isPressed())
            {
                afkTimer.reset();
                break;
            }
        }
        if (afkTimer.passed(secondsConfig.getValue() * 1000) && actionTimer.passed(delayConfig.getValue()))
        {
            if (jumpConfig.getValue())
            {
                mc.player.jump();
            }
            if (rotateConfig.getValue())
            {
                setRotationClient(mc.player.getYaw() + (RANDOM.nextFloat(90.0f) * (RANDOM.nextBoolean() ? 1.0f : -1.0f)),
                        mc.player.getPitch() + (RANDOM.nextFloat(90.0f) * (RANDOM.nextBoolean() ? 1.0f : -1.0f)));
            }
            actionTimer.reset();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof ChatMessageS2CPacket packet
                && autoReplyConfig.getValue())
        {
            String[] words = packet.body().content().split(" ");
            if (words[1].startsWith("whispers:"))
            {
                ChatUtil.serverSendCommand("r [Shoreline] " + replyConfig.getValue());
            }
        }
    }
}

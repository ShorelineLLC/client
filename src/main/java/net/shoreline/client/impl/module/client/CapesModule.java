package net.shoreline.client.impl.module.client;

import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.CapesEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.packet.client.CPacketPing;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.mixin.accessor.AccessorGameOptions;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

public final class CapesModule extends ToggleModule
{
    public static CapesModule instance;

    public Config<Capes> clientConfig = register(new EnumConfig<>("Client", "Shows client capes", Capes.OFF, Capes.values()));
    Config<Boolean> optifineConfig = register(new BooleanConfig("Optifine", "Shows optifine capes", true));

    private boolean capesEnabled;

    public CapesModule()
    {
        super("Capes", "Shows player capes", ModuleCategory.CLIENT);
        enable();
        instance = this;
    }

    @Override
    public void onEnable()
    {
        if (mc.options == null)
        {
            return;
        }
        capesEnabled = ((AccessorGameOptions) mc.options).getPlayerModelParts().contains(PlayerModelPart.CAPE);
        mc.options.togglePlayerModelPart(PlayerModelPart.CAPE, true);
    }

    @Override
    public void onDisable()
    {
        if (mc.options == null)
        {
            return;
        }
        mc.options.togglePlayerModelPart(PlayerModelPart.CAPE, capesEnabled);
    }

    @EventListener
    public void onGameJoinEvent(GameJoinEvent event)
    {
        onEnable();
    }

    @EventListener
    public void onCapes(CapesEvent event)
    {
        if (!optifineConfig.getValue() && clientConfig.getValue() == Capes.OFF)
        {
            return;
        }
        event.cancel();
        event.setShowOptifine(optifineConfig.getValue());
        OnlineUser onlineUser = IRCManager.getInstance().findOnlineUser(event.getGameProfile().getName());
        if (onlineUser != null && onlineUser.getCapeColor() != Capes.OFF)
        {
            String capePath = getCapePath(onlineUser);
            event.setTexture(Identifier.of("shoreline", capePath));
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getConfig() == clientConfig && event.getStage() == StageEvent.EventStage.POST)
        {
            IRCManager.getInstance().sendPingPacket();
        }
    }

    private String getCapePath(OnlineUser onlineUser)
    {
        StringBuilder capePath = new StringBuilder("cape");
        switch (onlineUser.getCapeColor())
        {
            case WHITE -> capePath.append("/white_bg");
            case BLACK -> capePath.append("/black_bg");
        }
        switch (onlineUser.getUsertype())
        {
            case RELEASE -> capePath.append("/white.png");
            case BETA -> capePath.append("/blue.png");
            case DEV -> capePath.append("/red.png");
        }
        return capePath.toString();
    }

    public enum Capes
    {
        WHITE,
        BLACK,
        OFF
    }
}

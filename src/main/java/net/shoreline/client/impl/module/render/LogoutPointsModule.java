package net.shoreline.client.impl.module.render;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogoutPointsModule extends RenderModule
{
    Config<Boolean> showDistance = new BooleanConfig.Builder("Distance")
            .setDescription("Shows distance from the logout")
            .setDefaultValue(false).build();
    Config<Boolean> showTimePassed = new BooleanConfig.Builder("TimePassed")
            .setDescription("Shows time passed since logout")
            .setDefaultValue(false).build();

    private final Map<UUID, LogoutData> onlinePlayers = Maps.newConcurrentMap();
    private final Map<UUID, LogoutData> offlinePlayers = Maps.newConcurrentMap();

    public LogoutPointsModule()
    {
        super("LogoutPoints", new String[] {"LogoutSpots"}, "Marks nearby logouts", GuiCategory.RENDER);
    }

    @Override
    public void onDisable()
    {
        onlinePlayers.clear();
        offlinePlayers.clear();
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        for (PlayerEntity player : mc.world.getPlayers())
        {
            if (player != null && player.getGameProfile() != null && !player.equals(mc.player))
            {
                onlinePlayers.put(player.getGameProfile().getId(), new LogoutData(player));
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof PlayerListS2CPacket packet
                && packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER))
        {
            for (PlayerListS2CPacket.Entry player : packet.getPlayerAdditionEntries())
            {
                for (UUID uuid : offlinePlayers.keySet())
                {
                    if (uuid.equals(player.profile().getId()) && offlinePlayers.containsKey(uuid))
                    {
                        LogoutData data = offlinePlayers.get(uuid);
                        data.setState(false);
                    }
                }
            }

            onlinePlayers.clear();
        }

        else if (event.getPacket() instanceof PlayerRemoveS2CPacket(List<UUID> profileIds))
        {
            for (UUID uuid2 : profileIds)
            {
                for (UUID uuid : onlinePlayers.keySet())
                {
                    if (!uuid.equals(uuid2))
                    {
                        continue;
                    }

                    LogoutData data = onlinePlayers.get(uuid);
                    if (!offlinePlayers.containsKey(uuid))
                    {
                        offlinePlayers.put(uuid, data);
                        data.setState(true);
                    }
                }
            }

            onlinePlayers.clear();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        for (UUID uuid : offlinePlayers.keySet())
        {
            LogoutData data = offlinePlayers.get(uuid);
            if (data == null)
            {
                continue;
            }

            double factor = data.getAnimation().getFactor();
            if (!data.getAnimation().getState() && factor <= 0.01f)
            {
                offlinePlayers.remove(uuid);
                return;
            }

            PlayerEntity offlinePlayer = data.getOfflinePlayer();
            StringBuilder logoutTag = new StringBuilder(offlinePlayer.getName().getString() + "'s Logout");

            boolean hasExtraInfo = false;
            if (showDistance.getValue())
            {
                hasExtraInfo = true;
                double dist = Math.sqrt(mc.player.squaredDistanceTo(offlinePlayer.getPos()));
                logoutTag.append(" - ").append(DECIMAL.format(dist)).append("m");
            }

            if (showTimePassed.getValue())
            {
                if (!hasExtraInfo)
                {
                    logoutTag.append(" -");
                }

                long seconds = (System.currentTimeMillis() - data.getLogoutTime()) / 1000;
                if (seconds < 60)
                {
                    logoutTag.append(" ").append(seconds).append("s");
                } else
                {
                    long minutes = seconds / 60;
                    logoutTag.append(" ").append(minutes).append("min");
                }
            }

            double animFactor = Easing.SMOOTH_STEP.ease(factor);
            Box box = offlinePlayer.getBoundingBox();
            BoxRender.OUTLINE.render(event.getMatrixStack(), box,
                    ColorUtil.withTransparency(ThemeModule.INSTANCE.getPrimaryColor(), (float) animFactor));

            Managers.RENDER.renderNametag(event.getMatrixStack(),
                    box.getHorizontalCenter().add(0.0f, box.getLengthY() + 0.2f, 0.0f),
                    0.003f,
                    logoutTag.toString(),
                    ColorUtil.withTransparency(Colors.WHITE, (float) animFactor));
        }
    }

    @Getter
    private static class LogoutData
    {
        private final PlayerEntity offlinePlayer;
        private final long logoutTime;
        private final Animation animation;

        public LogoutData(PlayerEntity offlinePlayer)
        {
            this.offlinePlayer = offlinePlayer;
            this.logoutTime = System.currentTimeMillis();
            this.animation = new Animation(false, 500L);
        }

        public void setState(boolean state)
        {
            this.animation.setState(state);
        }
    }
}

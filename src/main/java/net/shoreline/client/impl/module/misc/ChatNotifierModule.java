package net.shoreline.client.impl.module.misc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author linus & hockeyl8
 * @since 1.0
 */
public final class ChatNotifierModule extends ToggleModule
{
    Config<Boolean> totemPopConfig = register(new BooleanConfig("TotemPop", "Notifies in chat when a player pops a totem", true));
    Config<Boolean> visualRangeConfig = register(new BooleanConfig("VisualRange", "Notifies in chat when player enters visual range", false));
    Config<Boolean> joinConfig = register(new BooleanConfig("Join", "Notifies in chat when a player joins", false));
    Config<Boolean> leaveConfig = register(new BooleanConfig("Leave", "Notifies in chat when a player leaves", false));
    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Notifies for friends", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Notifies you if the server you join is running Grim", false));

    public ChatNotifierModule()
    {
        super("ChatNotifier", "Notifies in chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && totemPopConfig.getValue())
        {
            Entity entity = packet.getEntity(mc.world);
            if (!(entity instanceof LivingEntity))
            {
                return;
            }
            int totems = Managers.TOTEM.getTotems(entity);
            String playerName = entity.getName().getString();
            boolean isFriend = Managers.SOCIAL.isFriend(playerName);
            if (isFriend && !friendsConfig.getValue() || entity == mc.player)
            {
                return;
            }
            ChatUtil.clientSendMessage((isFriend ? "§g" : "§7") + playerName + "§f popped §s" + totems + "§f totems", entity.hashCode());
        }
        if (event.getPacket() instanceof PlayerListS2CPacket packet && joinConfig.getValue())
        {
            if (!(packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) || mc.player.age < 20)
            {
                return;
            }
            packet.getEntries().stream()
                    .filter(data -> data != null && data.profile() != null)
                    .filter(data -> data.profile().getName() != null && !data.profile().getName().isEmpty() || data.profile().getId() != null)
                    .forEach(data ->
                    {
                        String name = data.profile().getName();
                        if (name != null)
                        {
                            boolean isFriend = Managers.SOCIAL.isFriend(name);
                            if (!friendsConfig.getValue() && isFriend)
                            {
                                return;
                            }
                            ChatUtil.clientSendMessage((isFriend ? "§g" : "§7") + name + "§f has joined the server", name.hashCode());
                        }
                    });
        }
        if (event.getPacket() instanceof PlayerRemoveS2CPacket packet && leaveConfig.getValue())
        {
            if (mc.getNetworkHandler() == null)
            {
                return;
            }
            for (UUID uuid : packet.profileIds())
            {
                String name = null;
                for (PlayerListEntry info : new ArrayList<>(mc.getNetworkHandler().getPlayerList()))
                {
                    if (info == null)
                    {
                        continue;
                    }
                    GameProfile gameProfile = info.getProfile();
                    if (gameProfile.getId().equals(uuid))
                    {
                        name = gameProfile.getName();
                    }
                }
                if (name != null)
                {
                    boolean isFriend = Managers.SOCIAL.isFriend(name);
                    if (!friendsConfig.getValue() && isFriend)
                    {
                        return;
                    }
                    ChatUtil.clientSendMessage((isFriend ? "§g" : "§7") + name + "§f has left the server", name.hashCode());
                }
            }
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event)
    {
        if (!visualRangeConfig.getValue() || !(event.getEntity() instanceof PlayerEntity playerEntity) || event.getEntity() instanceof FakePlayerEntity)
        {
            return;
        }
        String playerName = event.getEntity().getName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player || playerName.equalsIgnoreCase("ShaderPlayer"))
        {
            return;
        }
        mc.executeSync(() -> ChatUtil.clientSendMessageRaw("§s[VisualRange] " + (isFriend ? "§g" : "§7") + playerName + "§f entered your visual range", playerEntity.hashCode()));
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (!visualRangeConfig.getValue() || !(event.getEntity() instanceof PlayerEntity playerEntity) || event.getEntity() instanceof FakePlayerEntity)
        {
            return;
        }
        String playerName = event.getEntity().getName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player || playerName.equalsIgnoreCase("ShaderPlayer"))
        {
            return;
        }
        mc.executeSync(() -> ChatUtil.clientSendMessageRaw("§s[VisualRange] " + (isFriend ? "§g" : "§c") + playerName + "§f left your visual range", playerEntity.hashCode()));
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (!totemPopConfig.getValue())
        {
            return;
        }
        int totems = Managers.TOTEM.getTotems(event.getEntity());
        if (totems == 0)
        {
            return;
        }
        String playerName = event.getEntity().getName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player)
        {
            return;
        }
        ChatUtil.clientSendMessage((isFriend ? "§g" : "§7") + playerName + "§f died after popping §s" + totems + "§f totems", event.getEntity().hashCode());
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        if (grimConfig.getValue() && !mc.isInSingleplayer())
        {
            if (Managers.ANTICHEAT.isGrim())
            {
                ChatUtil.clientSendMessage("This server is running GrimAC", 102);
            }
            else
            {
                ChatUtil.clientSendMessage("This server is not running GrimAC", 102);
            }
        }
    }
}

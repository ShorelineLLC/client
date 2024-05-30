package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.chat.ChatUtil;

public class ChatNotifierModule extends ToggleModule {

    Config<Boolean> totemPopConfig = register(new BooleanConfig("TotemPop", "Notifies in chat when a player pops a totem", true));
    Config<Boolean> visualRangeConfig = register(new BooleanConfig("VisualRange", "Notifies in chat when player enters visual range", false));
    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Notifies for friends", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Notifies you if the server you join is running Grim", false));

    public ChatNotifierModule() {
        super("ChatNotifier", "Notifies in chat", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && totemPopConfig.getValue()) {
            Entity entity = packet.getEntity(mc.world);
            if (!(entity instanceof LivingEntity) || entity.getDisplayName() == null) {
                return;
            }
            int totems = Managers.TOTEM.getTotems(entity);
            String playerName = entity.getDisplayName().getString();
            boolean isFriend = Managers.SOCIAL.isFriend(playerName);
            if (isFriend && !friendsConfig.getValue() || entity == mc.player) {
                return;
            }
            ChatUtil.clientSendMessage((isFriend ? "§b" : "§s") + playerName + "§f popped §s" + totems + "§f totems", 100);
        }
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        if (grimConfig.getValue())
        {
            if (Managers.ANTICHEAT.isGrim())
            {
                ChatUtil.clientSendMessage("This server is running GrimAC.", 101);
            }
            else
            {
                ChatUtil.clientSendMessage("This server is not running GrimAC.", 101);
            }
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event) {
        if (!visualRangeConfig.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player) {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[VisualRange] " + (isFriend ? "§b" + playerName : playerName) + "§f entered your visual range", 102);
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event) {
        if (!visualRangeConfig.getValue() || !(event.getEntity() instanceof PlayerEntity) || event.getEntity().getDisplayName() == null) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player) {
            return;
        }
        ChatUtil.clientSendMessageRaw("§s[VisualRange] " + (isFriend ? "§b" + playerName : "§c" + playerName) + "§f left your visual range", 102);
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getDisplayName() == null || !totemPopConfig.getValue()) {
            return;
        }
        int totems = Managers.TOTEM.getTotems(event.getEntity());
        if (totems == 0) {
            return;
        }
        String playerName = event.getEntity().getDisplayName().getString();
        boolean isFriend = Managers.SOCIAL.isFriend(playerName);
        if (isFriend && !friendsConfig.getValue() || event.getEntity() == mc.player) {
            return;
        }
        ChatUtil.clientSendMessage((isFriend ? "§b" : "§s") + playerName + "§f died after popping §s" + totems + "§f totems", 100);
    }
}

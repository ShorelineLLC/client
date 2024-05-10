package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;

public class PacketLoggerModule extends ToggleModule {

    Config<Boolean> chatConfig = new BooleanConfig("LogChat", "Logs packets in the chats", false);
    Config<Boolean> moveConfig = new BooleanConfig("PlayerMove", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> playerActionConfig = new BooleanConfig("PlayerAction", "Logs PlayerActionC2SPacket", false);
    Config<Boolean> updateSlotConfig = new BooleanConfig("UpdateSelectedSlot", "Logs UpdateSelectedSlotC2SPacket", false);
    Config<Boolean> handSwingConfig = new BooleanConfig("HandSwing", "Logs HandSwingC2SPacket", false);
    Config<Boolean> pongConfig = new BooleanConfig("Pong", "Logs CommonPongC2SPacket", false);

    public PacketLoggerModule() {
        super("PacketLogger", "Logs client packets", ModuleCategory.MISCELLANEOUS);
    }

    private void logPacket(String msg, Object... args) {
        String s = String.format(msg, args);
        if (chatConfig.getValue()) {
            sendModuleMessage(s);
        } else {
            System.out.println(s);
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && moveConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove - ");
            if (packet.changesPosition()) {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            if (packet.changesLook()) {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket packet && playerActionConfig.getValue()) {
            logPacket("PlayerAction - action: %s, direction: %s, pos: %s", packet.getAction().name(), packet.getDirection().name(), packet.getPos().toShortString());
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet && updateSlotConfig.getValue()) {
            logPacket("UpdateSlot - slot: %d", packet.getSelectedSlot());
        }
        if (event.getPacket() instanceof HandSwingC2SPacket packet && handSwingConfig.getValue()) {
            logPacket("HandSwing - hand: %s", packet.getHand().name());
        }
        if (event.getPacket() instanceof CommonPongC2SPacket packet && pongConfig.getValue()) {
            logPacket("Pong - %d", packet.getParameter());
        }
    }
}

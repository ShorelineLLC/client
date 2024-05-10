package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.hit.BlockHitResult;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2SPacket;

public class PacketLoggerModule extends ToggleModule {

    Config<Boolean> chatConfig = new BooleanConfig("LogChat", "Logs packets in the chats", false);
    Config<Boolean> moveFullConfig = new BooleanConfig("PlayerMoveFull", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> moveLookConfig = new BooleanConfig("PlayerMoveLook", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> movePosConfig = new BooleanConfig("PlayerMovePosition", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> moveGroundConfig = new BooleanConfig("PlayerMoveGround", "Logs PlayerMoveC2SPacket", false);
    Config<Boolean> playerActionConfig = new BooleanConfig("PlayerAction", "Logs PlayerActionC2SPacket", false);
    Config<Boolean> updateSlotConfig = new BooleanConfig("UpdateSelectedSlot", "Logs UpdateSelectedSlotC2SPacket", false);
    Config<Boolean> handSwingConfig = new BooleanConfig("HandSwing", "Logs HandSwingC2SPacket", false);
    Config<Boolean> interactEntityConfig = new BooleanConfig("InteractEntity", "Logs PlayerInteractEntityC2SPacket", false);
    Config<Boolean> interactBlockConfig = new BooleanConfig("InteractBlock", "Logs PlayerInteractBlockC2SPacket", false);
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
        if (event.getPacket() instanceof PlayerMoveC2SPacket.Full packet && moveFullConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove Full - ");
            if (packet.changesPosition()) {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            if (packet.changesLook()) {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround packet && movePosConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove PosGround - ");
            if (packet.changesPosition()) {
                builder.append("x: ").append(packet.getX(0.0)).append(", y: ").append(packet.getY(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround packet && moveLookConfig.getValue()) {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerMove LookGround - ");
            if (packet.changesLook()) {
                builder.append("yaw: ").append(packet.getYaw(0.0f)).append(", pitch: ").append(packet.getPitch(0.0f)).append(" ");
            }
            builder.append(" onground: ").append(packet.isOnGround());
            logPacket(builder.toString());
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly packet && moveGroundConfig.getValue()) {
            String s = "PlayerMove Ground - onground: " + packet.isOnGround();
            logPacket(s);
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
        if (event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet && interactEntityConfig.getValue()) {
            logPacket("InteractEntity - %s", packet.getEntity().getName().getString());
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && interactBlockConfig.getValue()) {
            BlockHitResult blockHitResult = packet.getBlockHitResult();
            logPacket("InteractBlock - pos: %s, dir: %s, hand: %s", blockHitResult.getBlockPos().toShortString(), blockHitResult.getSide().name(), packet.getHand().name());
        }
    }
}

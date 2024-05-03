package net.shoreline.client.impl.module.movement;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.FakePlayerEntity;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author linus
 * @since 1.0
 */
public class FakeLagModule extends ToggleModule {

    //
    Config<LagMode> modeConfig = new EnumConfig<>("Mode", "The mode for caching packets", LagMode.BLINK, LagMode.values());
    Config<Boolean> pulseConfig = new BooleanConfig("Pulse", "Releases packets at intervals", false);
    Config<Float> factorConfig = new NumberConfig<>("Factor", "The factor for packet intervals", 0.0f, 1.0f, 10.0f, () -> pulseConfig.getValue());
    Config<Boolean> renderConfig = new BooleanConfig("Render", "Renders the serverside player position", true);
    //
    private FakePlayerEntity serverModel;
    //
    private boolean blinking;
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();

    /**
     *
     */
    public FakeLagModule() {
        super("FakeLag", "Withholds packets from the server, creating clientside lag", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (renderConfig.getValue()) {
            serverModel = new FakePlayerEntity(mc.player, mc.getGameProfile());
            serverModel.despawnPlayer();
            serverModel.spawnPlayer();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        if (!packets.isEmpty()) {
            for (Packet<?> p : packets) {
                Managers.NETWORK.sendPacket(p);
            }
            packets.clear();
        }
        if (serverModel != null) {
            serverModel.despawnPlayer();
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == EventStage.PRE && pulseConfig.getValue() && packets.size() > factorConfig.getValue() * 10.0f) {
            blinking = true;
            if (!packets.isEmpty()) {
                for (Packet<?> p : packets) {
                    Managers.NETWORK.sendPacket(p);
                }
            }
            packets.clear();
            if (serverModel != null) {
                serverModel.copyPositionAndRotation(mc.player);
                serverModel.setHeadYaw(mc.player.headYaw);
            }
            blinking = false;
        }
    }

    @EventListener
    public void onDisconnectEvent(DisconnectEvent event) {
        // packets.clear();
        disable();
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null || mc.player.isRiding() || blinking) {
            return;
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket
                || event.getPacket() instanceof ClientCommandC2SPacket || event.getPacket() instanceof HandSwingC2SPacket
                || event.getPacket() instanceof PlayerInteractEntityC2SPacket || event.getPacket() instanceof PlayerInteractBlockC2SPacket
                || event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            event.cancel();
            packets.add(event.getPacket());
        }
    }

    public enum LagMode {
        BLINK
    }
}

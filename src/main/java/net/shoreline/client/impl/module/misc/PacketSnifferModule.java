package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.Packet;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

public class PacketSnifferModule extends Toggleable
{
    Config<Boolean> logPacket = new BooleanConfig.Builder("Log")
            .setDescription("Logs the packets in chat")
            .setDefaultValue(true).build();
    Config<Boolean> logPacketSpam = new BooleanConfig.Builder("PacketSpam")
            .setDescription("Detects and logs potential packet spam")
            .setDefaultValue(true).build();
    Config<Boolean> cancelPacket = new BooleanConfig.Builder("Cancel")
            .setDescription("Cancels the packets from sending/recieving")
            .setDefaultValue(false).build();

    Config<Boolean> moveFullConfig = new BooleanConfig.Builder("PlayerMoveFull")
            .setDescription("Logs PlayerMoveC2SPacket").setDefaultValue(false).build();
    Config<Boolean> moveLookConfig = new BooleanConfig.Builder("PlayerMoveLook")
            .setDescription("Logs PlayerMoveC2SPacket").setDefaultValue(false).build();
    Config<Boolean> movePosConfig = new BooleanConfig.Builder("PlayerMovePosition")
            .setDescription("Logs PlayerMoveC2SPacket").setDefaultValue(false).build();
    Config<Boolean> moveGroundConfig = new BooleanConfig.Builder("PlayerMoveGround")
            .setDescription("Logs PlayerMoveC2SPacket").setDefaultValue(false).build();
    Config<Boolean> vehicleMoveConfig = new BooleanConfig.Builder("VehicleMove")
            .setDescription("Logs VehicleMoveC2SPacket").setDefaultValue(false).build();
    Config<Boolean> playerActionConfig = new BooleanConfig.Builder("PlayerAction")
            .setDescription("Logs PlayerActionC2SPacket").setDefaultValue(false).build();
    Config<Boolean> updateSlotConfig = new BooleanConfig.Builder("UpdateSelectedSlot")
            .setDescription("Logs UpdateSelectedSlotC2SPacket").setDefaultValue(false).build();
    Config<Boolean> clickSlotConfig = new BooleanConfig.Builder("ClickSlot")
            .setDescription("Logs ClickSlotC2SPacket").setDefaultValue(false).build();
    Config<Boolean> pickInventoryConfig = new BooleanConfig.Builder("PickInventory")
            .setDescription("Logs PickFromInventoryC2SPacket").setDefaultValue(false).build();
    Config<Boolean> handSwingConfig = new BooleanConfig.Builder("HandSwing")
            .setDescription("Logs HandSwingC2SPacket").setDefaultValue(false).build();
    Config<Boolean> interactEntityConfig = new BooleanConfig.Builder("InteractEntity")
            .setDescription("Logs PlayerInteractEntityC2SPacket").setDefaultValue(false).build();
    Config<Boolean> interactBlockConfig = new BooleanConfig.Builder("InteractBlock")
            .setDescription("Logs PlayerInteractBlockC2SPacket").setDefaultValue(false).build();
    Config<Boolean> interactItemConfig = new BooleanConfig.Builder("InteractItem")
            .setDescription("Logs PlayerInteractItemC2SPacket").setDefaultValue(false).build();
    Config<Boolean> commandConfig = new BooleanConfig.Builder("ClientCommand")
            .setDescription("Logs ClientCommandC2SPacket").setDefaultValue(false).build();
    Config<Boolean> statusConfig  = new BooleanConfig.Builder("ClientStatus")
            .setDescription("Logs ClientStatusC2SPacket").setDefaultValue(false).build();
    Config<Boolean> closeScreenConfig  = new BooleanConfig.Builder("CloseScreen")
            .setDescription("Logs CloseHandledScreenC2SPacket").setDefaultValue(false).build();
    Config<Boolean> teleportConfirmConfig = new BooleanConfig.Builder("TeleportConfirm")
            .setDescription("Logs TeleportConfirmC2SPacket").setDefaultValue(false).build();
    Config<Boolean> pongConfig = new BooleanConfig.Builder("Pong")
            .setDescription("Logs CommonPongC2SPacket").setDefaultValue(false).build();
    Config<Void> clientPacketGroup = new ConfigGroup.Builder("Outgoing")
            .addAll(moveFullConfig, moveLookConfig, movePosConfig, moveGroundConfig, vehicleMoveConfig,
                    playerActionConfig, updateSlotConfig, clickSlotConfig, pickInventoryConfig,
                    handSwingConfig, interactEntityConfig, interactBlockConfig, interactItemConfig,
                    commandConfig, statusConfig, closeScreenConfig, teleportConfirmConfig, pongConfig).build();

    Config<Boolean> positionLookConfig = new BooleanConfig.Builder("PlayerPositionLook")
            .setDescription("Logs PlayerPositionLookS2CPacket").setDefaultValue(false).build();
    Config<Boolean> velocityConfig = new BooleanConfig.Builder("EntityVelocityUpdate")
            .setDescription("Logs EntityVelocityUpdateS2CPacket").setDefaultValue(false).build();
    Config<Boolean> explosionConfig = new BooleanConfig.Builder("Explosion")
            .setDescription("Logs ExplosionS2CPacket").setDefaultValue(false).build();
    Config<Boolean> trackerUpdateConfig = new BooleanConfig.Builder("EntityTrackerUpdate")
            .setDescription("Logs EntityTrackerUpdateS2CPacket").setDefaultValue(false).build();
    Config<Boolean> statusEffectConfig = new BooleanConfig.Builder("EntityStatusEffect")
            .setDescription("Logs EntityStatusEffectS2CPacket").setDefaultValue(false).build();
    Config<Boolean> entityStatusConfig = new BooleanConfig.Builder("EntityStatus")
            .setDescription("Logs EntityStatusS2CPacket").setDefaultValue(false).build();
    Config<Boolean> gameStateChangeConfig = new BooleanConfig.Builder("GameStateChange")
            .setDescription("Logs GameStateChangeS2CPacket").setDefaultValue(false).build();
    Config<Boolean> abilitiesConfig = new BooleanConfig.Builder("PlayerAbilities")
            .setDescription("Logs PlayerAbilitiesS2CPacket").setDefaultValue(false).build();
    Config<Boolean> healthUpdateConfig = new BooleanConfig.Builder("HealthUpdate")
            .setDescription("Logs HealthUpdateS2CPacket").setDefaultValue(false).build();
    Config<Boolean> cameraEntityConfig = new BooleanConfig.Builder("SetCameraEntity")
            .setDescription("Logs SetCameraEntityS2CPacket").setDefaultValue(false).build();
    Config<Boolean> keepAliveConfig = new BooleanConfig.Builder("KeepAlive")
            .setDescription("Logs KeepAliveS2CPacket").setDefaultValue(false).build();
    Config<Boolean> simulationDistanceConfig = new BooleanConfig.Builder("SimulationDistance")
            .setDescription("Logs SimulationDistanceS2CPacket").setDefaultValue(false).build();
    Config<Void> serverPacketGroup = new ConfigGroup.Builder("Incoming")
            .addAll(positionLookConfig, velocityConfig, explosionConfig, trackerUpdateConfig,
                    statusEffectConfig, entityStatusConfig, gameStateChangeConfig, abilitiesConfig,
                    healthUpdateConfig, cameraEntityConfig, keepAliveConfig, simulationDistanceConfig).build();

    public PacketSnifferModule()
    {
        super("PacketSniffer", new String[] {"PacketLogger"},
                "Logs client packets", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onDisconnect(WorldEvent.Disconnect event)
    {
        if (!logPacketSpam.getValue())
        {
            return;
        }

        StringBuilder log = new StringBuilder();
        for (NetworkHandler handler : Managers.NETWORK.getHandlers())
        {
            long packets = Managers.NETWORK.getPacketsSent(handler);

            if (packets == 0)
            {
                continue;
            }

            log.append(handler.getName())
                    .append(": ")
                    .append(packets)
                    .append(", ");
        }

        Shoreline.info(log.toString());
    }

    public String formatPacket(Packet<?> packet)
    {
        return null;
    }
}

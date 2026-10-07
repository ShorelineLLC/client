package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.combat.trap.TrapLayer;
import net.shoreline.client.impl.module.combat.trap.TrapModule;
import net.shoreline.client.impl.module.combat.trap.TrapSpec;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.EnumSet;

public class SelfTrapModule extends TrapModule
{
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Boolean> extendFeet = new BooleanConfig.Builder("ExtendFeet")
            .setDescription("Extends feet trap when being mined")
            .setDefaultValue(false).build();
    Config<Boolean> extendBody = new BooleanConfig.Builder("ExtendBody")
            .setDescription("Extends body trap when being mined")
            .setDefaultValue(false).build();
    Config<Boolean> headConfig = new BooleanConfig.Builder("CoverHead")
            .setDescription("Traps player head")
            .setDefaultValue(false).build();

    Config<Boolean> instantReplace = new BooleanConfig.Builder("Instant")
            .setDescription("Replaces instantly after mined")
            .setDefaultValue(false).build();
    Config<Boolean> sequentialReplace = new BooleanConfig.Builder("Sequential")
            .setDescription("Replaces instantly after explosions")
            .setDefaultValue(false).build();
    Config<Boolean> attackSequential = new BooleanConfig.Builder("Attack")
            .setDescription("Attacks crystals when they spawn")
            .setVisible(() -> sequentialReplace.getValue())
            .setDefaultValue(false).build();
    Config<Void> replaceConfig = new ConfigGroup.Builder("Replace")
            .addAll(instantReplace, sequentialReplace, attackSequential).build();

    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Disables when player y-level changes")
            .setDefaultValue(false).build();

    private double prevY;

    public SelfTrapModule()
    {
        super("SelfTrap", "Traps the player", GuiCategory.COMBAT);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull())
        {
            prevY = mc.player.getY();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        double dy = mc.player.getY() - prevY;
        if (autoDisable.getValue() && (dy > 0.5 || dy < -1.5))
        {
            disable();
            return;
        }

        int obbySlot = findBestObbySlot();
        if (obbySlot == -1)
        {
            return;
        }

        final Box playerBox = mc.player.getBoundingBox();
        Box boundingBox = playerBox.withMinY(Math.round(playerBox.minY)).shrink(0.01, 0.1, 0.01);
        TrapSpec trapSpec = TrapSpec.builder()
                .layers(getLayers())
                .extendFeet(extendFeet.getValue())
                .extendBody(extendBody.getValue())
                .build();

        trapPos.calcTrap(boundingBox, trapSpec);

        createPlacementsFromPositions(getCurrentObbyBlock(), trapPos.getTrapPositions(), placeRange.getValue());
        if (placements.isEmpty() || !Managers.INTERACT.startPlacement(obbySlot))
        {
            return;
        }

        for (BlockPos placement : placements)
        {
            placeObby(placement);
        }

        Managers.INTERACT.endPlacement();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet && packet.getState().isAir() && instantReplace.getValue())
        {
            BlockPos blockPos = packet.getPos();
            if (trapPos.getTrapPositions().contains(blockPos))
            {
                runSingleObbyPlacement(blockPos);
            }
        }

        else if (event.getPacket() instanceof EntitySpawnS2CPacket packet
                && packet.getEntityType() == EntityType.END_CRYSTAL && sequentialReplace.getValue())
        {
            BlockPos blockPos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());
            if (!trapPos.getTrapPositions().contains(blockPos))
            {
                return;
            }

            if (attackSequential.getValue())
            {
                sendAttackPacketsInternal(packet.getEntityId(), false, Hand.MAIN_HAND);
            }

            runSingleObbyPlacement(blockPos);
        }
    }

    @Override
    public EnumSet<TrapLayer> getLayers()
    {
        EnumSet<TrapLayer> layers = EnumSet.of(TrapLayer.FEET, TrapLayer.BODY);
        if (headConfig.getValue())
        {
            layers.add(TrapLayer.CEILING);
        }

        return layers;
    }
}

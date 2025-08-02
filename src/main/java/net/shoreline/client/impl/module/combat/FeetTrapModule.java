package net.shoreline.client.impl.module.combat;

import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
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
import java.util.List;

public class FeetTrapModule extends TrapModule
{
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Boolean> extendFeet = new BooleanConfig.Builder("Extend")
            .setDescription("Extends feet trap when being mined")
            .setDefaultValue(false).build();

    Config<Boolean> instantReplace = new BooleanConfig.Builder("Instant")
            .setDescription("Replaces instantly after mined")
            .setDefaultValue(false).build();
    Config<Boolean> sequentialReplace = new BooleanConfig.Builder("Sequential")
            .setDescription("Replaces instantly after explosions")
            .setDefaultValue(false).build();
    Config<Void> replaceConfig = new ConfigGroup.Builder("Replace")
            .addAll(instantReplace, sequentialReplace).build();

    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Disables when player y-level changes")
            .setDefaultValue(false).build();

    private double prevY;

    public FeetTrapModule()
    {
        super("FeetTrap", new String[] {"Surround"}, "Surrounds feet in obsidian", GuiCategory.COMBAT);
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
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        if (autoDisable.getValue() && (mc.player.getY() - prevY > 0.5 || mc.player.fallDistance > 1.5f))
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
        EnumSet<TrapLayer> layers = EnumSet.of(TrapLayer.FEET);
        if (headConfig.getValue())
        {
            layers.add(TrapLayer.HEAD);
        }

        TrapSpec trapSpec = TrapSpec.builder()
                .layers(layers)
                .extendFeet(extendFeet.getValue())
                .build();

        trapPos.calcTrap(boundingBox, trapSpec);

        List<BlockPos> placements = getPlacements(getCurrentObbyBlock(), trapPos.getTrapPositions(), placeRange.getValue());
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

        else if (event.getPacket() instanceof ExplosionS2CPacket packet && sequentialReplace.getValue())
        {
            BlockPos blockPos = BlockPos.ofFloored(packet.center());
            if (trapPos.getTrapPositions().contains(blockPos))
            {
                runSingleObbyPlacement(blockPos);
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        renderBlockPlacements(event.getMatrixStack());
    }
}

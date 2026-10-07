package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.entity.RenderItemFrameEvent;
import net.shoreline.client.impl.event.world.WorldSkylightEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.HashSet;
import java.util.Set;

public class NoLagModule extends Toggleable
{
    Config<Boolean> noSkyLightLag = new BooleanConfig.Builder("Skylight")
            .setDescription("Prevents lag from skylight updates")
            .setDefaultValue(false).build();
    Config<Boolean> noItemFrameLag = new BooleanConfig.Builder("ItemFrame")
            .setDescription("Prevents lag from item frames")
            .setDefaultValue(false).build();
    Config<Boolean> noParticleLag = new BooleanConfig.Builder("Particle")
            .setDescription("Prevents lag from particle entities")
            .setDefaultValue(false).build();
    Config<Boolean> noSoundLag = new BooleanConfig.Builder("Sound")
            .setDescription("Prevents lag from sounds in the world")
            .setDefaultValue(false).build();

    private final Set<SoundEvent> equipSounds = new HashSet<>(Set.of(
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(),
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value()
    ));

    public NoLagModule()
    {
        super("NoLag", "Prevents attempts to intentionally lag the game", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onRenderSkylight(WorldSkylightEvent event)
    {
        if (noSkyLightLag.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderItemFrame(RenderItemFrameEvent event)
    {
        if (noItemFrameLag.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (noSoundLag.getValue() && event.getPacket() instanceof PlaySoundFromEntityS2CPacket packet && equipSounds.contains(packet.getSound().value()))
        {
            event.cancel();
        } else if (noParticleLag.getValue() && event.getPacket() instanceof ParticleS2CPacket packet && packet.getCount() > 512)
        {
            event.cancel();
        }
    }
}

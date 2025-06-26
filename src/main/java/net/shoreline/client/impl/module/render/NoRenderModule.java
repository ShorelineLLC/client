package net.shoreline.client.impl.module.render;

import net.minecraft.item.Items;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.gui.hud.OverlayEvent;
import net.shoreline.client.impl.event.render.RenderFloatingItemEvent;
import net.shoreline.client.impl.event.render.RenderNauseaEvent;
import net.shoreline.client.impl.event.render.TiltViewEvent;
import net.shoreline.client.impl.event.render.entity.feature.RenderArmorEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoRenderModule extends Toggleable
{
    Config<Boolean> hurtCamConfig = new BooleanConfig.Builder("HurtCam")
            .setDescription("Cancels the camera shake when taking damage")
            .setDefaultValue(true).build();
    Config<Boolean> armorConfig = new BooleanConfig.Builder("Armor")
            .setDescription("Removes armor rendering")
            .setDefaultValue(false).build();
    Config<Boolean> fireOverlay = new BooleanConfig.Builder("Fire")
            .setDescription("Cancels the burning screen overlay")
            .setDefaultValue(true).build();
    Config<Boolean> waterOverlay = new BooleanConfig.Builder("Water")
            .setDescription("Cancels the water screen overlay")
            .setDefaultValue(true).build();
    Config<Boolean> frostbiteOverlay = new BooleanConfig.Builder("Frostbite")
            .setDescription("Cancels the water screen overlay")
            .setDefaultValue(true).build();
    Config<Boolean> blockOverlay = new BooleanConfig.Builder("Blocks")
            .setDescription("Cancels the block screen overlay")
            .setDefaultValue(true).build();
    Config<Boolean> spyglassOverlay = new BooleanConfig.Builder("Spyglass")
            .setDescription("Cancels the spyglass overlay")
            .setDefaultValue(false).build();
    Config<Boolean> bossBarOverlay = new BooleanConfig.Builder("BossBar")
            .setDescription("Cancels the boss bar screen overlay")
            .setDefaultValue(false).build();
    Config<Boolean> portalOverlay = new BooleanConfig.Builder("Portal")
            .setDescription("Cancels the nether portal screen overlay")
            .setDefaultValue(false).build();
    Config<Void> overlayConfig = new ConfigGroup.Builder("Overlays")
            .addAll(fireOverlay, waterOverlay, frostbiteOverlay, blockOverlay,
                    spyglassOverlay, bossBarOverlay, portalOverlay).build();
    Config<FogRender> fogConfig = new EnumConfig.Builder<FogRender>("Fog")
            .setValues(FogRender.values())
            .setDescription("Prevents fog from rendering in the world")
            .setDefaultValue(FogRender.CLEAR).build();
    Config<Boolean> nauseaConfig = new BooleanConfig.Builder("Nausea")
            .setDescription("Cancels the nausea effect")
            .setDefaultValue(false).build();
    Config<Boolean> blindnessConfig = new BooleanConfig.Builder("Blindness")
            .setDescription("Cancels the blindness effect")
            .setDefaultValue(false).build();
    Config<Boolean> totemConfig = new BooleanConfig.Builder("Totem")
            .setDescription("Cancels the totem pop animation")
            .setDefaultValue(false).build();

    public NoRenderModule()
    {
        super("NoRender", "Prevents certain game elements from rendering", GuiCategory.RENDER);
    }

    @EventListener
    public void onTiltView(TiltViewEvent event)
    {
        if (hurtCamConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderArmor(RenderArmorEvent event)
    {
        if (armorConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onFireOverlay(OverlayEvent.Fire event)
    {
        if (fireOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onWaterOverlay(OverlayEvent.Water event)
    {
        if (waterOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onBlocksOverlay(OverlayEvent.Blocks event)
    {
        if (blockOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onFrostbiteOverlay(OverlayEvent.Frostbite event)
    {
        if (frostbiteOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onSpyglassOverlay(OverlayEvent.Spyglass event)
    {
        if (spyglassOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPortalOverlay(OverlayEvent.Portal event)
    {
        if (portalOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onBossBarOverlay(OverlayEvent.BossBar event)
    {
        if (bossBarOverlay.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderNausea(RenderNauseaEvent event)
    {
        if (nauseaConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFloatingItem(RenderFloatingItemEvent event)
    {
        if (totemConfig.getValue() && event.getStack().getItem() == Items.TOTEM_OF_UNDYING)
        {
            event.cancel();
        }
    }

    public enum FogRender
    {
        CLEAR,
        LIQUID_VISION,
        OFF
    }
}

package net.shoreline.client.impl.module.render;

import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.event.gui.hud.OverlayEvent;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.client.impl.event.render.GlyphShadowEvent;
import net.shoreline.client.impl.event.render.RenderFloatingItemEvent;
import net.shoreline.client.impl.event.render.RenderNauseaEvent;
import net.shoreline.client.impl.event.render.TiltViewEvent;
import net.shoreline.client.impl.event.render.entity.feature.RenderArmorEvent;
import net.shoreline.client.impl.event.toast.RenderGuiToastEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoRenderModule extends Toggleable
{
    public static NoRenderModule INSTANCE;

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

    Config<Boolean> explosionsConfig = new BooleanConfig.Builder("Explosion")
            .setDescription("Cancels the explosion particles")
            .setDefaultValue(false).build();
    Config<Boolean> effectsConfig = new BooleanConfig.Builder("StatusEffect")
            .setDescription("Cancels the potion effect particles")
            .setDefaultValue(false).build();
    Config<Boolean> splashConfig = new BooleanConfig.Builder("BottleSplash")
            .setDescription("Cancels the bottle splash particles")
            .setDefaultValue(false).build();
    Config<Boolean> portalConfig = new BooleanConfig.Builder("Portal")
            .setDescription("Cancels the portal particles")
            .setDefaultValue(false).build();
    Config<Boolean> drippingBlocksConfig = new BooleanConfig.Builder("DrippingBlocks")
            .setDescription("Cancels the block dripping particles")
            .setDefaultValue(false).build();
    Config<Boolean> walkingConfig = new BooleanConfig.Builder("Walking")
            .setDescription("Cancels the walking particles")
            .setDefaultValue(false).build();
    Config<Boolean> eatingConfig = new BooleanConfig.Builder("Eating")
            .setDescription("Cancels the eating particles")
            .setDefaultValue(false).build();
    Config<Boolean> breakingConfig = new BooleanConfig.Builder("Breaking")
            .setDescription("Cancels the block breaking particles")
            .setDefaultValue(false).build();
    Config<Void> particlesConfig = new ConfigGroup.Builder("Particles")
            .addAll(explosionsConfig, effectsConfig, splashConfig, portalConfig,
                    drippingBlocksConfig, walkingConfig, eatingConfig, breakingConfig).build();

    Config<Boolean> potionsHud = new BooleanConfig.Builder("Effects")
            .setDescription("Cancels the status effects hud element")
            .setDefaultValue(false).build();
    Config<Boolean> itemName = new BooleanConfig.Builder("ItemName")
            .setDescription("Cancels the item name hud element")
            .setDefaultValue(false).build();
    Config<Boolean> toastConfig = new BooleanConfig.Builder("Toast")
            .setDescription("Cancels the toast hud element")
            .setDefaultValue(true).build();
    Config<Boolean> textShadow = new BooleanConfig.Builder("TextShadow")
            .setDescription("Reduces the vanilla text shadow")
            .setDefaultValue(false).build();
    Config<Void> hudConfig = new ConfigGroup.Builder("HUD")
            .addAll(potionsHud, itemName, toastConfig, textShadow).build();

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
        INSTANCE = this;
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
    public void onParticle(ParticleEvent event)
    {
        if (event.getParticleEffect() == ParticleTypes.ENTITY_EFFECT && effectsConfig.getValue()
                || event.getParticleEffect() == ParticleTypes.EXPLOSION && explosionsConfig.getValue()
                || (event.getParticleEffect() == ParticleTypes.EFFECT || event.getParticleEffect() == ParticleTypes.INSTANT_EFFECT) && splashConfig.getValue()
                || event.getParticleEffect() == ParticleTypes.PORTAL && portalConfig.getValue()
                || event.getParticleEffect() == ParticleTypes.BLOCK && walkingConfig.getValue()
                || event.getParticleEffect() == ParticleTypes.ITEM && eatingConfig.getValue()
                || (event.getParticleEffect() == ParticleTypes.FALLING_OBSIDIAN_TEAR || event.getParticleEffect() == ParticleTypes.DRIPPING_OBSIDIAN_TEAR || event.getParticleEffect() == ParticleTypes.LANDING_OBSIDIAN_TEAR
                || event.getParticleEffect() == ParticleTypes.FALLING_DRIPSTONE_WATER || event.getParticleEffect() == ParticleTypes.DRIPPING_DRIPSTONE_WATER || event.getParticleEffect() == ParticleTypes.FALLING_DRIPSTONE_LAVA
                || event.getParticleEffect() == ParticleTypes.DRIPPING_DRIPSTONE_LAVA || event.getParticleEffect() == ParticleTypes.FALLING_LAVA || event.getParticleEffect() == ParticleTypes.DRIPPING_LAVA
                || event.getParticleEffect() == ParticleTypes.FALLING_WATER || event.getParticleEffect() == ParticleTypes.DRIPPING_WATER || event.getParticleEffect() == ParticleTypes.FALLING_HONEY
                || event.getParticleEffect() == ParticleTypes.DRIPPING_HONEY || event.getParticleEffect() == ParticleTypes.FALLING_NECTAR) && drippingBlocksConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPotionsHudOverlay(HudOverlayEvent.Potions event)
    {
        if (potionsHud.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onItemNameHudOverlay(HudOverlayEvent.ItemName event)
    {
        if (itemName.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderGuiToast(RenderGuiToastEvent event)
    {
        if (toastConfig.getValue())
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

    @EventListener
    public void onGlyphShadow(GlyphShadowEvent event)
    {
        if (textShadow.getValue())
        {
            event.cancel();
            event.setShadowOffset(0.5f);
        }
    }

    public boolean hidePotionHud()
    {
        return potionsHud.getValue();
    }

    public enum FogRender
    {
        CLEAR,
        LIQUID_VISION,
        OFF
    }
}

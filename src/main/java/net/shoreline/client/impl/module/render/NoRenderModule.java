package net.shoreline.client.impl.module.render;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.entity.EntityHurtEvent;
import net.shoreline.client.impl.event.entity.RenderOnFireEvent;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.event.gui.hud.OverlayEvent;
import net.shoreline.client.impl.event.particle.BlockBreakParticleEvent;
import net.shoreline.client.impl.event.particle.EmitParticleEvent;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.client.impl.event.render.*;
import net.shoreline.client.impl.event.render.entity.feature.RenderArmorEvent;
import net.shoreline.client.impl.event.toast.RenderGuiToastEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NoRenderModule extends RenderModule
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

    Config<Boolean> totemEffects = new BooleanConfig.Builder("TotemEffects")
            .setDescription("Cancels the totem effects when a player pops")
            .setDefaultValue(false).build();
    Config<Integer> totemParticles = new NumberConfig.Builder<Integer>("TotemParticles")
            .setMin(3).setDefaultValue(16).setMax(16)
            .setVisible(() -> !totemEffects.getValue())
            .setDescription("The number of particles for the totem effect").build();
    Config<Integer> totemTicks = new NumberConfig.Builder<Integer>("TotemTicks")
            .setMin(5).setDefaultValue(30).setMax(30)
            .setVisible(() -> !totemEffects.getValue())
            .setDescription("The time in ticks that the totem effect will last").build();
    Config<Boolean> fireEffect = new BooleanConfig.Builder("EntityFire")
            .setDescription("Cancels the fire effect on entities")
            .setDefaultValue(false).build();
    Config<Boolean> hurt = new BooleanConfig.Builder("Hurt")
            .setDescription("Cancels the red effect when you hurt a entity")
            .setDefaultValue(false).build();
    Config<Void> effectConfig = new ConfigGroup.Builder("Effects")
            .addAll(totemEffects, totemParticles, totemTicks, fireEffect, hurt).build();

    Config<Boolean> explosionsConfig = new BooleanConfig.Builder("Explosion")
            .setDescription("Cancels the explosion particles")
            .setDefaultValue(false).build();
    Config<Boolean> statusEffectsConfig = new BooleanConfig.Builder("StatusEffect")
            .setDescription("Cancels the potion effect particles")
            .setDefaultValue(false).build();
    Config<Boolean> fireworkConfig = new BooleanConfig.Builder("Firework")
            .setDescription("Cancels the firework particles")
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
            .addAll(explosionsConfig, statusEffectsConfig, fireworkConfig, splashConfig, portalConfig,
                    drippingBlocksConfig, walkingConfig, eatingConfig, breakingConfig).build();

    @Getter
    Config<Boolean> potionsHud = new BooleanConfig.Builder("PotionEffects")
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

    Config<Collection<Block>> blockBlackListConfig = new RegistryConfig.Builder<Block>("Blacklist")
            .setValues(Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT)
            .setRegistry(Registries.BLOCK)
            .setDescription("List of blocks that you dont want to render")
            .build();
    Config<Boolean> blocksConfig = new ToggleableConfigGroup.Builder("Blocks")
            .add(blockBlackListConfig)
            .setDefaultValue(false).build();
    Config<Boolean> nauseaConfig = new BooleanConfig.Builder("Nausea")
            .setDescription("Cancels the nausea effect")
            .setDefaultValue(false).build();
    Config<Boolean> blindnessConfig = new BooleanConfig.Builder("Blindness")
            .setDescription("Cancels the blindness effect")
            .setDefaultValue(false).build();
    Config<Boolean> totemConfig = new BooleanConfig.Builder("Totem")
            .setDescription("Cancels the totem pop animation")
            .setDefaultValue(false).build();

    private final Set<ParticleType<?>> drippingParticles = new HashSet<>(Set.of(
            ParticleTypes.FALLING_OBSIDIAN_TEAR,
            ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
            ParticleTypes.LANDING_OBSIDIAN_TEAR,
            ParticleTypes.FALLING_DRIPSTONE_WATER,
            ParticleTypes.DRIPPING_DRIPSTONE_WATER,
            ParticleTypes.FALLING_DRIPSTONE_LAVA,
            ParticleTypes.DRIPPING_DRIPSTONE_LAVA,
            ParticleTypes.FALLING_LAVA,
            ParticleTypes.DRIPPING_LAVA,
            ParticleTypes.FALLING_WATER,
            ParticleTypes.DRIPPING_WATER,
            ParticleTypes.FALLING_HONEY,
            ParticleTypes.DRIPPING_HONEY,
            ParticleTypes.FALLING_NECTAR
    ));

    public NoRenderModule()
    {
        super("NoRender", "Prevents certain game elements from rendering", GuiCategory.RENDER);
        INSTANCE = this;
        blockBlackListConfig.addObserver(v -> reload(true));
        blocksConfig.addObserver(v -> reload(true));
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
        if (shouldCancelParticle(event.getParticleType()))
        {
            event.cancel();
        }
    }

    @EventListener
    public void onBlockBreakParticle(BlockBreakParticleEvent event)
    {
        if (breakingConfig.getValue())
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
    public void onRenderEntityFire(RenderOnFireEvent event)
    {
        if (fireEffect.getValue())
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

    @EventListener
    public void onEmitParticle(EmitParticleEvent event)
    {
        if (event.getEffect() != ParticleTypes.TOTEM_OF_UNDYING)
        {
            return;
        }

        if (totemEffects.getValue())
        {
            event.cancel();
            event.setMaxCount(0);
            return;
        }

        event.setMaxCount(totemParticles.getValue());
        event.setMaxTicks(totemTicks.getValue());
    }

    @EventListener
    public void onEntityHurt(EntityHurtEvent event)
    {
        if (hurt.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderBlock(RenderBlockEvent event)
    {
        if (!blocksConfig.getValue())
        {
            return;
        }

        Block block = event.getState().getBlock();
        if (blockBlackListConfig.getValue().contains(block))
        {
            event.cancel();
        }
    }

    private boolean shouldCancelParticle(ParticleType<?> type)
    {
        return type == ParticleTypes.ENTITY_EFFECT && statusEffectsConfig.getValue()
                || (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER) && explosionsConfig.getValue()
                || type == ParticleTypes.FIREWORK && fireworkConfig.getValue()
                || (type == ParticleTypes.EFFECT || type == ParticleTypes.INSTANT_EFFECT) && splashConfig.getValue()
                || (type == ParticleTypes.PORTAL || type == ParticleTypes.REVERSE_PORTAL) && portalConfig.getValue()
                || type == ParticleTypes.BLOCK && walkingConfig.getValue()
                || type == ParticleTypes.ITEM && eatingConfig.getValue()
                || drippingParticles.contains(type) && drippingBlocksConfig.getValue();
    }
}

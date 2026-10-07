package net.shoreline.client.impl.module.hud;

import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.module.client.HudGuiModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class PotionsHudModule extends DynamicHudModule
{
    Config<PotionColors> potionColors = new EnumConfig.Builder<PotionColors>("Color")
            .setValues(PotionColors.values())
            .setDescription("The potion colors in hud")
            .setDefaultValue(PotionColors.DEFAULT).build();
    Config<Boolean> potionIcons = new BooleanConfig.Builder("Icons")
            .setDescription("Shows the potion icon")
            .setDefaultValue(false).build();
//    Config<Collection<StatusEffect>> blacklist = new RegistryConfig.Builder<StatusEffect>("Blacklist")
//            .setRegistry(Registries.STATUS_EFFECT)
//            .setDescription("What potions to blacklist")
//            .build();

    private final Map<StatusEffect, String> nameMap = new HashMap<>();

    public PotionsHudModule()
    {
        super("Potions", "Displays the status effects the local player has", 80, 80);
    }

    @Override
    public void onEnable() {}

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        loadEntries();
    }

    @Override
    public void loadEntries()
    {
        for (StatusEffect effect : Registries.STATUS_EFFECT)
        {
            RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.getEntry(effect);
            getHudEntries().add(new DynamicPotionEntry(
                    this,
                    mc.getStatusEffectSpriteManager().getSprite(entry),
                    () -> decorate(entry, effect),
                    () -> mc.player != null && mc.player.hasStatusEffect(entry),
                    () -> getPotionColor(entry, effect)));
        }
    }

    @Override
    public void onDisable() {}

    @Override
    public void sortEntries()
    {
        getHudEntries().sort(Comparator.comparing(entry -> entry.getText().get()));
    }

    @Override
    public float getWidth()
    {
        return super.getWidth() + (potionIcons.getValue() ? 13 : 0);
    }

    public String decorate(RegistryEntry<StatusEffect> entry, StatusEffect effect)
    {
        if (mc.player.getStatusEffect(entry) != null)
        {
            StatusEffectInstance instance = mc.player.getStatusEffect(entry);
            String decorated = effect.getName().getString() + (instance.getAmplifier() > 0
                    ? " " + (instance.getAmplifier() + 1)
                    : "") + " " + Formatting.WHITE + getPotionDuration(instance);
            nameMap.put(effect, decorated);
            return decorated;
        }

        String str = nameMap.get(effect);
        return str == null ? effect.getName().getString() : str;
    }

    private String getPotionDuration(StatusEffectInstance instance)
    {
        if (instance.isInfinite())
        {
            return "*:*";
        }
        else
        {
            int duration = instance.getDuration();
            int mins = duration / 1200;
            int sec = (duration % 1200) / 20;
            return mins + ":" + (sec < 10 ? "0" + sec : sec);
        }
    }

    public int getPotionColor(RegistryEntry<StatusEffect> entry, StatusEffect effect)
    {
        if (potionColors.getValue() == PotionColors.DEFAULT)
        {
            return effect.getColor();
        } else if (potionColors.getValue() == PotionColors.THEME)
        {
            return ThemeModule.COLOR;
        }

        String id = entry.getIdAsString();
        return switch (id.replace("minecraft:", ""))
        {
            case "speed" -> 8171462;
            case "slowness" -> 5926017;
            case "haste" -> 14270531;
            case "mining_fatigue" -> 4866583;
            case "strength" -> 9643043;
            case "instant_health" -> 16262179;
            case "instant_damage" -> 4393481;
            case "jump_boost" -> 2293580;
            case "nausea" -> 5578058;
            case "regeneration" -> 13458603;
            case "resistance" -> 10044730;
            case "fire_resistance" -> 14981690;
            case "water_breathing" -> 3035801;
            case "invisibility" -> 8356754;
            case "blindness" -> 2039587;
            case "night_vision" -> 2039713;
            case "hunger" -> 5797459;
            case "weakness" -> 4738376;
            case "poison" -> 5149489;
            case "wither" -> 3484199;
            case "health_boost" -> 16284963;
            case "absorption" -> 2445989;
            case "saturation" -> 16262179;
            case "glowing" -> 9740385;
            case "levitation" -> 13565951;
            case "luck" -> 3381504;
            case "unluck" -> 12624973;
            default -> effect.getColor();
        };
    }

    private class DynamicPotionEntry extends DynamicEntry
    {
        private final Sprite sprite;
        private final Supplier<Integer> color;

        public DynamicPotionEntry(DynamicHudModule mod, Sprite sprite, Supplier<String> text, Supplier<Boolean> drawing, Supplier<Integer> color)
        {
            super(mod, text, drawing);
            this.sprite = sprite;
            this.color = color;
        }

        @Override
        public void drawText(DrawContext context, String string, float x, float y)
        {
            MatrixStack matrices = context.getMatrices();
            if (sprite != null && potionIcons.getValue())
            {
                matrices.push();
                matrices.translate(x - 12.5f, y - 2.5f, 0.0f);
                context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, 0, 0, 11, 11);
                matrices.pop();
            }

            int c = color.get();
            if (c == ThemeModule.COLOR)
            {
                getModule().drawTextTransparency(matrices, string, x, y, (float) yAnimation.getFactor());
                return;
            }

            c = ColorUtil.withTransparency(new Color(c), 1.0f);
            getModule().drawTextTransparency(matrices, string, x, y, c, (float) yAnimation.getFactor());
        }
    }

    private enum PotionColors
    {
        DEFAULT, OLD, THEME
    }
}

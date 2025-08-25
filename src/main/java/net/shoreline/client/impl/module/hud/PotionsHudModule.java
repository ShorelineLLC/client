package net.shoreline.client.impl.module.hud;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.render.ColorUtil;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PotionsHudModule extends DynamicHudModule
{
    private final Map<StatusEffect, String> nameMap = new HashMap<>();

    public PotionsHudModule()
    {
        super("Potions", "Displays the status effects the local player has", 80, 80);
    }

    @Override
    public void onEnable()
    {
        for (StatusEffect effect : Registries.STATUS_EFFECT)
        {
            getHudEntries().add(new DynamicPotionEntry(
                    this,
                    () -> decorate(effect),
                    () -> mc.player != null && mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(effect)),
                    effect.getColor()));
        }
    }

    @Override
    public void sortEntries()
    {
        getHudEntries().sort(Comparator.comparing(entry -> entry.getText().get()));
    }

    public String decorate(StatusEffect effect)
    {
        if (mc.player.getStatusEffect(Registries.STATUS_EFFECT.getEntry(effect)) != null)
        {
            StatusEffectInstance instance = mc.player.getStatusEffect(Registries.STATUS_EFFECT.getEntry(effect));
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

    private static class DynamicPotionEntry extends DynamicEntry
    {
        private final int color;

        public DynamicPotionEntry(DynamicHudModule mod, Supplier<String> text, Supplier<Boolean> drawing, int color)
        {
            super(mod, text, drawing);
            this.color = ColorUtil.withTransparency(new Color(color), 1.0f);
        }

        @Override
        public void drawText(MatrixStack matrices, String string, float x, float y)
        {
            getModule().drawText(matrices, string, x, y, color);
        }
    }
}

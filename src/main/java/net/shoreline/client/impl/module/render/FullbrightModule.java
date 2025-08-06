package net.shoreline.client.impl.module.render;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.WorldGammaEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class FullbrightModule extends Toggleable
{
    Config<Brightness> modeConfig = new EnumConfig.Builder<Brightness>("Mode")
            .setValues(Brightness.values())
            .setDescription("The client world brightness mode")
            .setDefaultValue(Brightness.GAMMA).build();

    private final RegistryEntry<StatusEffect> fullBrightEffect;

    public FullbrightModule()
    {
        super("Fullbright", "Brightens the world", GuiCategory.RENDER);
        this.fullBrightEffect = Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of("shoreline", "full_bright"),
                new FullBrightEffect()
        );
    }

    @Override
    public void onEnable()
    {
        if (!checkNull() && modeConfig.getValue() == Brightness.POTION)
        {
            mc.player.addStatusEffect(new StatusEffectInstance(fullBrightEffect, -1, 0)); // INFINITE
        }
    }

    @Override
    public void onDisable()
    {
        if (!checkNull() && mc.player.hasStatusEffect(fullBrightEffect) && modeConfig.getValue() == Brightness.POTION)
        {
            mc.player.removeStatusEffect(fullBrightEffect);
        }
    }

    @EventListener
    public void onWorldGamma(WorldGammaEvent event)
    {
        if (modeConfig.getValue() == Brightness.GAMMA)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (modeConfig.getValue() == Brightness.POTION)
        {
            if (!mc.player.hasStatusEffect(fullBrightEffect))
            {
                mc.player.addStatusEffect(new StatusEffectInstance(fullBrightEffect, -1, 0)); // INFINITE
            }
        }
        else if (mc.player.hasStatusEffect(fullBrightEffect))
        {
            mc.player.removeStatusEffect(fullBrightEffect);
        }
    }

    public enum Brightness
    {
        GAMMA,
        POTION
    }

    private static class FullBrightEffect extends StatusEffect
    {
        public FullBrightEffect()
        {
            super(StatusEffectCategory.BENEFICIAL, 0xffffffff);

        }

        @Override
        public boolean applyUpdateEffect(ServerWorld world,
                                         LivingEntity entity,
                                         int amplifier)
        {
            return StatusEffects.NIGHT_VISION.value().applyUpdateEffect(world, entity, amplifier);
        }

        @Override
        public boolean canApplyUpdateEffect(int duration,
                                            int amplifier)
        {
            return true;
        }

        @Override
        public Text getName()
        {
            return Text.of("Fullbright");
        }
    }
}

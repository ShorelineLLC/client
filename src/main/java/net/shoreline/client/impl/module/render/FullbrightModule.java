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
import net.shoreline.eventbus.event.StageEvent;

public class FullbrightModule extends Toggleable
{
    Config<Brightness> modeConfig = new EnumConfig.Builder<Brightness>("Mode")
            .setValues(Brightness.values())
            .setDescription("The client world brightness mode")
            .setDefaultValue(Brightness.GAMMA).build();

    private static final RegistryEntry<StatusEffect> FULL_BRIGHT = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of("shoreline", "full_bright"),
            new FullBrightEffect()
    );

    public FullbrightModule()
    {
        super("Fullbright", "Brightens the world", GuiCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull() && modeConfig.getValue() == Brightness.POTION)
        {
            mc.player.addStatusEffect(new StatusEffectInstance(FULL_BRIGHT, -1, 0)); // INFINITE
        }
    }

    @Override
    public void onDisable()
    {
        if (!checkNull() && mc.player.hasStatusEffect(FULL_BRIGHT) && modeConfig.getValue() == Brightness.POTION)
        {
            mc.player.removeStatusEffect(FULL_BRIGHT);
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
    public void onTick(TickEvent event)
    {
        if (checkNull() || event.getStage() != StageEvent.EventStage.POST)
        {
            return;
        }

        if (modeConfig.getValue() == Brightness.POTION)
        {
            if (!mc.player.hasStatusEffect(FULL_BRIGHT))
            {
                mc.player.addStatusEffect(new StatusEffectInstance(FULL_BRIGHT, -1, 0)); // INFINITE
            }
        }
        else if (mc.player.hasStatusEffect(FULL_BRIGHT))
        {
            mc.player.removeStatusEffect(FULL_BRIGHT);
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

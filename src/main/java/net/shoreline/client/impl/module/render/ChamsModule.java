package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.loader.Loader;
import net.shoreline.loader.session.UserSession;

public class ChamsModule extends Toggleable
{
    private static ChamsModule INSTANCE;
    public Config<Boolean> renderPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Render players").setDefaultValue(true).build();
    public Config<Boolean> renderHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Render hostiles").setDefaultValue(false).build();
    public Config<Boolean> renderPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Render passives").setDefaultValue(false).build();
    public Config<Boolean> renderCrystals = new BooleanConfig.Builder("Crystals")
            .setDescription("Render crystals").setDefaultValue(false).build();
    public Config<Void> renderConfig = new ConfigGroup.Builder("Target")
            .addAll(renderPlayers, renderHostiles, renderPassives, renderCrystals).build();
    public Config<Boolean> shine = new BooleanConfig.Builder("Shine")
            .setDefaultValue(false).build();
    public Config<Float> scale = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f)
            .setVisible(shine::getValue).build();
    public Config<Float> speed = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(0.5f)
            .setVisible(shine::getValue).build();
    public final Config<Boolean> xqz = new BooleanConfig.Builder("XQZ")
            .setDefaultValue(false).build();
    public Config<Float> opacity = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(1.0f)
            .setVisible(xqz::getValue).build();

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", GuiCategory.RENDER);
        INSTANCE = this;
    }

    public boolean isValid(Entity entity)
    {
        if (entity == mc.player)
        {
            return false;
        }

        if (entity instanceof PlayerEntity && renderPlayers.getValue())
        {
            return true;
        }
        else if (entity instanceof Monster && renderHostiles.getValue())
        {
            return true;
        }
        else if (entity instanceof AnimalEntity && renderPassives.getValue())
        {
            return true;
        }

        return entity instanceof EndCrystalEntity && renderCrystals.getValue();
    }

    public static ChamsModule getInstance()
    {
        return INSTANCE;
    }

    public float getSpeed()
    {
        return speed.getValue();
    }

    public float getScale()
    {
        return scale.getValue();
    }
}

package net.shoreline.client.impl.module.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class EntityModule extends Toggleable
{
    public Config<Boolean> players = new BooleanConfig.Builder("Players")
            .setDescription("Target Players").setDefaultValue(true).build();
    public Config<Boolean> hostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Target Hostiles").setDefaultValue(false).build();
    public Config<Boolean> passives = new BooleanConfig.Builder("Passives")
            .setDescription("Target Passives").setDefaultValue(false).build();
    public Config<Boolean> crystals = new BooleanConfig.Builder("Crystals")
            .setDescription("Target Crystals").setDefaultValue(false).build();

    public EntityModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public EntityModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public boolean isValid(Entity entity)
    {
        if (entity == null || entity == mc.player)
        {
            return false;
        }

        return switch (entity)
        {
            case PlayerEntity player when players.getValue() -> true;
            case Monster monster when hostiles.getValue() -> true;
            case AnimalEntity animalEntity when passives.getValue() -> true;
            default -> entity instanceof EndCrystalEntity && crystals.getValue();
        };
    }
}
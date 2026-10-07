package net.shoreline.client.impl.module.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.util.entity.EntityUtil;

public class TargetingModule extends ListeningToggleable
{
    public Config<Boolean> targetPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Target Players").setDefaultValue(true).build();
    public Config<Boolean> targetHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Target Hostiles").setDefaultValue(false).build();
    public Config<Boolean> targetPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Target Passives").setDefaultValue(false).build();

    public TargetingModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public TargetingModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public boolean isValid(EntityType<?> entityType)
    {
        return entityType == EntityType.PLAYER && targetPlayers.getValue()
                || EntityUtil.isHostile(entityType) && targetHostiles.getValue()
                || EntityUtil.isPassive(entityType) && targetPassives.getValue();
    }

    public boolean isValid(Entity entity)
    {
        if (entity == null || entity == mc.player)
        {
            return false;
        }

        return switch (entity)
        {
            case PlayerEntity player when targetPlayers.getValue() -> true;
            case Monster monster when targetHostiles.getValue() -> true;
            default -> entity instanceof AnimalEntity && targetPassives.getValue();
        };
    }
}
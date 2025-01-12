package net.shoreline.client.impl.module;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.init.Managers;

import java.util.Comparator;
import java.util.function.Predicate;

public class CombatModule extends RotationModule
{

    public CombatModule(String name, String desc, ModuleCategory category)
    {
        super(name, desc, category);
    }

    public CombatModule(String name, String desc, ModuleCategory category, int rotationPriority)
    {
        super(name, desc, category, rotationPriority);
    }

    public PlayerEntity getClosestPlayer(double range)
    {
        return mc.world.getPlayers().stream().filter(e -> !(e instanceof ClientPlayerEntity) && !e.isSpectator())
                .filter(e -> mc.player.squaredDistanceTo(e) <= range * range)
                .filter(e -> !Managers.SOCIAL.isFriend(e.getName().getString()))
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e))).orElse(null);
    }

    public PlayerEntity getClosestPlayer(Predicate<AbstractClientPlayerEntity> entityPredicate, double range)
    {
        return mc.world.getPlayers().stream().filter(e -> !(e instanceof ClientPlayerEntity) && !e.isSpectator())
                .filter(entityPredicate)
                .filter(e -> mc.player.squaredDistanceTo(e) <= range * range)
                .filter(e -> !Managers.SOCIAL.isFriend(e.getName().getString()))
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e))).orElse(null);
    }
}

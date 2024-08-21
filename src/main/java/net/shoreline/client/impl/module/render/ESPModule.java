package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.EntityOutlineEvent;
import net.shoreline.client.impl.event.entity.decoration.TeamColorEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class ESPModule extends ToggleModule
{
    //
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The ESP render range", 10.0f, 50.0f, 200.0f));
    Config<ESPMode> modeConfig = register(new EnumConfig<>("Mode", "ESP rendering mode", ESPMode.GLOW, ESPMode.values()));
    Config<Float> widthConfig = register(new NumberConfig<>("Linewidth", "ESP rendering line width", 0.1f, 1.25f, 5.0f));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render players through walls", true));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render self through walls", true));
    Config<Color> playersColorConfig = register(new ColorConfig("PlayersColor", "The render color for players", new Color(200, 60, 60), false, () -> playersConfig.getValue() || selfConfig.getValue()));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render monsters through walls", true));
    Config<Color> monstersColorConfig = register(new ColorConfig("MonstersColor", "The render color for monsters", new Color(200, 60, 60), false, () -> monstersConfig.getValue()));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render animals through walls", true));
    Config<Color> animalsColorConfig = register(new ColorConfig("AnimalsColor", "The render color for animals", new Color(0, 200, 0), false, () -> animalsConfig.getValue()));
    Config<Boolean> vehiclesConfig = register(new BooleanConfig("Vehicles", "Render vehicles through walls", false));
    Config<Color> vehiclesColorConfig = register(new ColorConfig("VehiclesColor", "The render color for vehicles", new Color(200, 100, 0), false, () -> vehiclesConfig.getValue()));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Render dropped items through walls", false));
    Config<Color> itemsColorConfig = register(new ColorConfig("ItemsColor", "The render color for items", new Color(200, 100, 0), false, () -> itemsConfig.getValue()));
    Config<Boolean> crystalsConfig = register(new BooleanConfig("EndCrystals", "Render end crystals through walls", false));
    Config<Color> crystalsColorConfig = register(new ColorConfig("EndCrystalsColor", "The render color for end crystals", new Color(200, 100, 200), false, () -> crystalsConfig.getValue()));

    public ESPModule()
    {
        super("ESP", "See entities and objects through walls", ModuleCategory.RENDER);
    }

    @EventListener
    public void onEntityOutline(EntityOutlineEvent event)
    {
        if (mc.player != null && modeConfig.getValue() == ESPMode.GLOW && checkESP(event.getEntity()))
        {
            if (mc.player.squaredDistanceTo(event.getEntity()) > ((NumberConfig) rangeConfig).getValueSq())
            {
                return;
            }
            event.cancel();
        }
    }

    @EventListener
    public void onTeamColor(TeamColorEvent event)
    {
        if (mc.player != null && modeConfig.getValue() == ESPMode.GLOW && checkESP(event.getEntity()))
        {
            if (mc.player.squaredDistanceTo(event.getEntity()) > ((NumberConfig) rangeConfig).getValueSq())
            {
                return;
            }
            event.cancel();
            event.setColor(getESPColor(event.getEntity()).getRGB());
        }
    }

    public Color getESPColor(Entity entity)
    {
        if (entity instanceof PlayerEntity player)
        {
            if (Managers.SOCIAL.isFriend(player.getName()))
            {
                return new Color(0xff66ffff);
            }
            return playersColorConfig.getValue();
        }
        if (EntityUtil.isMonster(entity))
        {
            return monstersColorConfig.getValue();
        }
        if (EntityUtil.isNeutral(entity) || EntityUtil.isPassive(entity))
        {
            return animalsColorConfig.getValue();
        }
        if (EntityUtil.isVehicle(entity))
        {
            return vehiclesColorConfig.getValue();
        }
        if (entity instanceof EndCrystalEntity)
        {
            return crystalsColorConfig.getValue();
        }
        if (entity instanceof ItemEntity)
        {
            return itemsColorConfig.getValue();
        }
        return null;
    }

    public boolean checkESP(Entity entity)
    {
        if (entity instanceof PlayerEntity && playersConfig.getValue())
        {
            return selfConfig.getValue() || entity != mc.player;
        }
        return EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animalsConfig.getValue()
                || EntityUtil.isVehicle(entity) && vehiclesConfig.getValue()
                || entity instanceof EndCrystalEntity && crystalsConfig.getValue()
                || entity instanceof ItemEntity && itemsConfig.getValue();
    }

    public enum ESPMode
    {
        // OUTLINE,
        GLOW
    }
}

package net.shoreline.client.impl.module.render;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ChamsRenderer;

import java.awt.*;

@Getter
public class ChamsModule extends Toggleable
{
    private static ChamsModule INSTANCE;
    public Config<ChamsMode> mode = new EnumConfig.Builder<ChamsMode>("Mode")
            .setValues(ChamsMode.values())
            .setDefaultValue(ChamsMode.CHAMS).build();
    public Config<Boolean> extraLayer = new BooleanConfig.Builder("ExtraLayer")
            .setDefaultValue(true).build();
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
    public Config<Boolean> throughWalls = new BooleanConfig.Builder("ThroughWalls")
            .setDefaultValue(true).build();
    public Config<Float> scale = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f)
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE).build();
    public Config<Float> speed = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(0.5f)
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE).build();
    public Config<Boolean> model = new BooleanConfig.Builder("Model")
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE)
            .setDefaultValue(false).build();
    public Config<Float> opacityConfig = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(1.0f)
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE && model.getValue()
                    || mode.getValue() == ChamsMode.XQZ).build();
    public Config<Color> color = new ColorConfig.Builder("Color")
            .setRgb(0xFFFFFFFF).setTransparency(true).build();

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", GuiCategory.RENDER);
        INSTANCE = this;
    }

    public float getOpacity()
    {
        if (mode.getValue() == ChamsMode.XQZ)
        {
            return opacityConfig.getValue();
        }
        else if (mode.getValue() == ChamsMode.SHINE && model.getValue())
        {
            return opacityConfig.getValue();
        }

        return 0.0f;
    }

    public boolean isValid(Entity entity)
    {
        if (entity == mc.player || !Managers.RENDER.isVisible(entity.getBoundingBox()))
        {
            return false;
        }

        return switch (entity)
        {
            case PlayerEntity player when renderPlayers.getValue() -> true;
            case Monster monster when renderHostiles.getValue() -> true;
            case AnimalEntity animalEntity when renderPassives.getValue() -> true;
            default -> entity instanceof EndCrystalEntity && renderCrystals.getValue();
        };

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

    @Getter
    public enum ChamsMode
    {
        NONE(ChamsRenderer.NONE),
        XQZ(ChamsRenderer.NONE),
        CHAMS(ChamsRenderer.CHAMS),
        WIRECHAMS(ChamsRenderer.BOTH),
        SHINE(ChamsRenderer.NONE);

        public final ChamsRenderer renderer;

        ChamsMode(ChamsRenderer renderer)
        {
            this.renderer = renderer;
        }
    }
}

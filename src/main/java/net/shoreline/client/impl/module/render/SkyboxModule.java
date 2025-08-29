package net.shoreline.client.impl.module.render;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.SkyboxEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class SkyboxModule extends Toggleable
{
    Config<FogMode> cancelFog = new EnumConfig.Builder<FogMode>("Fog")
            .setValues(FogMode.values())
            .setDescription("Prevents fog from rendering in the world")
            .setDefaultValue(FogMode.CLEAR).build();
    Config<Integer> fogDistance = new NumberConfig.Builder<Integer>("FogDistance")
            .setMin(1).setMax(256).setDefaultValue(120)
            .setVisible(() -> cancelFog.getValue() == FogMode.COLOR)
            .setDescription("The distance from the player that the fog will start").build();
    Config<Boolean> cancelSky = new BooleanConfig.Builder("Sky")
            .setDescription("Change how the sky is rendered in the world")
            .setDefaultValue(false).build();
    Config<Color> skyColor = new ColorConfig.Builder("SkyColor")
            .setDescription("The color of the sky")
            .setDefaultValue(Color.WHITE).build();
    Config<Color> cloudColor = new ColorConfig.Builder("CloudColor")
            .setDescription("The color of the clouds")
            .setDefaultValue(Color.WHITE).build();

    public SkyboxModule()
    {
        super("Skybox", "Changes the world skybox", GuiCategory.RENDER);
    }

    @EventListener
    public void onFogRender(SkyboxEvent.Fog event)
    {
        if (checkNull() || event.getType() != BackgroundRenderer.FogType.FOG_TERRAIN)
        {
            return;
        }

        if (cancelFog.getValue() == FogMode.CLEAR || mc.player.isSubmergedInWater() || mc.player.isSubmergedIn(FluidTags.LAVA))
        {
            event.cancel();
            event.setFogStart(event.getViewDist() * 4.0f);
            event.setFogEnd(event.getViewDist() * 4.25f);
        } else if (cancelFog.getValue() == FogMode.COLOR)
        {
            event.cancel();
            float f = MathHelper.clamp(256.0f - fogDistance.getValue(), 10.0f, 256.0f);
            event.setFogStart(event.getViewDist() - f);
            event.setFogEnd(event.getViewDist());
        }
    }

    @EventListener
    public void onFogColor(SkyboxEvent.FogColor event)
    {
        if (cancelFog.getValue() == FogMode.COLOR)
        {
            event.cancel();
            event.setColor(skyColor.getValue());
        }
    }

    @EventListener
    public void onSkyColor(SkyboxEvent.SkyColor event)
    {
        if (cancelSky.getValue())
        {
            event.cancel();
            event.setColor(skyColor.getValue());
        }
    }

    @EventListener
    public void onCloudColor(SkyboxEvent.CloudColor event)
    {
        event.cancel();
        event.setColor(cloudColor.getValue());
    }

    private enum FogMode
    {
        CLEAR,
        COLOR,
        OFF
    }
}

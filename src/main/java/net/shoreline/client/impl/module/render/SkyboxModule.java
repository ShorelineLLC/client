package net.shoreline.client.impl.module.render;

import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderFogEvent;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class SkyboxModule extends ToggleModule
{

    Config<Integer> dayTimeConfig = register(new NumberConfig<>("WorldTime", "The world time of day", 0, 6000, 24000));
    Config<Boolean> skyConfig = register(new BooleanConfig("Sky", "Changes the world skybox color", true));
    Config<Color> skyColorConfig = register(new ColorConfig("SkyColor", "The color for the world skybox", new Color(255, 0, 0), false, true, () -> skyConfig.getValue()));
    Config<Boolean> cloudConfig = register(new BooleanConfig("Cloud", "Changes the world cloud color", false));
    Config<Color> cloudColorConfig = register(new ColorConfig("CloudColor", "The color for the world clouds", new Color(255, 0, 0), false, true, () -> cloudConfig.getValue()));
    Config<Boolean> fogConfig = register(new BooleanConfig("Fog", "Changes the world fog color", false));
    Config<Float> fogStartConfig = register(new NumberConfig<>("FogStart", "The fog start distance", 0.0f, 0.0f, 256.0f, () -> fogConfig.getValue()));
    Config<Float> fogEndConfig = register(new NumberConfig<>("FogEnd", "The fog start distance", 10.0f, 64.0f, 256.0f, () -> fogConfig.getValue()));
    Config<Color> fogColorConfig = register(new ColorConfig("FogColor", "The color for the world fog", new Color(255, 0, 0), false, true, () -> fogConfig.getValue()));

    public SkyboxModule()
    {
        super("Skybox", "Changes the rendering of the world skybox", ModuleCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.POST)
        {
            mc.world.setTimeOfDay(dayTimeConfig.getValue());
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onSkyboxSky(SkyboxEvent.Sky event)
    {
        if (skyConfig.getValue())
        {
            event.cancel();
            event.setColor(skyColorConfig.getValue());
        }
    }

    @EventListener
    public void onSkyboxCloud(SkyboxEvent.Cloud event)
    {
        if (cloudConfig.getValue())
        {
            event.cancel();
            event.setColor(cloudColorConfig.getValue());
        }
    }

    @EventListener
    public void onSkyboxFog(SkyboxEvent.Fog event)
    {
        if (fogConfig.getValue())
        {
            event.cancel();
            event.setColor(fogColorConfig.getValue());
        }
    }

    @EventListener
    public void onRenderFog(RenderFogEvent event)
    {
        if (fogConfig.getValue())
        {
            event.cancel();
            event.setStart(fogStartConfig.getValue());
            event.setEnd(fogEndConfig.getValue());
        }
    }
}

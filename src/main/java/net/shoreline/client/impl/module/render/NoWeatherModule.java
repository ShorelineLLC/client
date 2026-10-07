package net.shoreline.client.impl.module.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Calendar;

public class NoWeatherModule extends Toggleable
{
    Config<Weather> weatherConfig = new EnumConfig.Builder<Weather>("Weather")
            .setValues(Weather.values())
            .setDescription("The client world weather")
            .setDefaultValue(Weather.CLEAR).build();
    Config<Time> timeConfig = new EnumConfig.Builder<Time>("Time")
            .setValues(Time.values())
            .setDescription("The client world time")
            .setDefaultValue(Time.AFTERNOON).build();

    private Weather prevWeather;
    private long prevTime;

    public NoWeatherModule()
    {
        super("NoWeather", "Disables client weather", GuiCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        if (checkNull())
        {
            return;
        }

        prevTime = mc.world.getLevelProperties().getTimeOfDay();
        if (mc.world.isThundering())
        {
            prevWeather = Weather.THUNDER;
        } else if (mc.world.isRaining())
        {
            prevWeather = Weather.RAIN;
        } else
        {
            prevWeather = Weather.CLEAR;
        }
    }

    @Override
    public void onDisable()
    {
        if (mc.world != null && prevWeather != null)
        {
            setWeather(prevWeather);
            mc.world.getLevelProperties().setTimeOfDay(prevTime);
        }
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (!checkNull())
        {
            setWeather(weatherConfig.getValue());
            mc.world.getLevelProperties().setTimeOfDay(timeConfig.getValue().getTime());
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof GameStateChangeS2CPacket packet
                && (packet.getReason() == GameStateChangeS2CPacket.RAIN_STARTED
                || packet.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED
                || packet.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED
                || packet.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED))
        {
            event.cancel();
        }

        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
        {
            event.cancel();
        }
    }

    private void setWeather(Weather weather)
    {
        switch (weather)
        {
            case CLEAR ->
            {
                mc.world.getLevelProperties().setRaining(false);
                mc.world.setRainGradient(0.0f);
                mc.world.setThunderGradient(0.0f);
            }
            case RAIN ->
            {
                mc.world.getLevelProperties().setRaining(true);
                mc.world.setRainGradient(1.0f);
                mc.world.setThunderGradient(0.0f);
            }
            case THUNDER ->
            {
                mc.world.getLevelProperties().setRaining(true);
                mc.world.setRainGradient(2.0f);
                mc.world.setThunderGradient(1.0f);
            }
        }
    }

    private enum Weather
    {
        CLEAR,
        RAIN,
        THUNDER
    }

    @RequiredArgsConstructor
    @Getter
    private enum Time
    {
        SUNRISE(0),
        MORNING(3000),
        NOON(6000),
        AFTERNOON(9000),
        SUNSET(12000),
        EVENING(15000),
        MIDNIGHT(18000);

        private final long time;
    }
}

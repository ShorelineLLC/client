package net.shoreline.client.impl.event.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.BackgroundRenderer;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;
import org.joml.Vector4f;

import java.awt.*;

@Getter
@Setter
public class SkyboxEvent extends Event
{
    private Color color;

    public Vector4f getColorVec4()
    {
        if (color == null)
        {
            return new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        return new Vector4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    @AllArgsConstructor
    @Cancelable
    @Getter
    @Setter
    public static class Fog extends Event {

        private final BackgroundRenderer.FogType type;
        private float viewDist;
        private float fogStart, fogEnd;
    }

    @Cancelable
    public static class FogColor extends SkyboxEvent {}

    @Cancelable
    public static class SkyColor extends SkyboxEvent {}

    @Cancelable
    public static class CloudColor extends SkyboxEvent {}
}

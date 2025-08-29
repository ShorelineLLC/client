package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;
import org.joml.Vector3f;

import java.awt.*;

@Getter
@Setter
public class WorldTintEvent extends Event
{
    private Color color;

    public Vector3f getColorVec3()
    {
        if (color == null)
        {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        return new Vector3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
    }

    @Cancelable
    public static class Light extends WorldTintEvent {}

    @Cancelable
    public static class Lava extends WorldTintEvent {}

    @Cancelable
    public static class Water extends WorldTintEvent {}

    @Cancelable
    public static class Foliage extends WorldTintEvent {}
}

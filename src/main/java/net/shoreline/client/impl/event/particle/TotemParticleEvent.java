package net.shoreline.client.impl.event.particle;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

import java.awt.*;

@Cancelable
@Getter
@Setter
public class TotemParticleEvent extends Event
{
    private Color color;
}

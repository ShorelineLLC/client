package net.shoreline.client.impl.event.entity;

import net.minecraft.util.math.Box;
import net.shoreline.client.api.event.Event;

public class SetBBEvent extends Event {

    private final Box boundingBox;

    public SetBBEvent(Box boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Box getBoundingBox() {
        return boundingBox;
    }
}

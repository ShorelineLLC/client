package net.shoreline.client.impl.event.gui.screen;

import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;
import net.shoreline.client.util.render.animation.Animation;

@Cancelable
public class RenderOpenChatEvent extends Event {

    private float animation;

    public void setAnimation(float animation) {
        this.animation = animation;
    }

    public float getAnimation() {
        return animation;
    }
}

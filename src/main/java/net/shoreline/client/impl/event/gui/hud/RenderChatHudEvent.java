package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;
import net.shoreline.client.util.render.animation.TimeAnimation;

@Cancelable
public class RenderChatHudEvent extends Event
{
    private final ChatHudLine chatHudLine;
    private double animation;

    public RenderChatHudEvent(ChatHudLine chatHudLine) {
        this.chatHudLine = chatHudLine;
    }

    public double getAnimation() {
        return animation;
    }

    public void setAnimation(double animation) {
        this.animation = animation;
    }

    public ChatHudLine getChatHudLine() {
        return chatHudLine;
    }
}

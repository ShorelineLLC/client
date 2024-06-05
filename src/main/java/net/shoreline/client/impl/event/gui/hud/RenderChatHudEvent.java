package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RenderChatHudEvent extends Event
{
    private final ChatHudLine chatHudLine;
    private double animation;
    private boolean animationMode;

    public RenderChatHudEvent(ChatHudLine chatHudLine)
    {
        this.chatHudLine = chatHudLine;
    }

    public double getAnimation()
    {
        return animation;
    }

    public void setAnimation(double animation)
    {
        this.animation = animation;
    }

    public void setSlide(boolean animationMode)
    {
        this.animationMode = animationMode;
    }

    public ChatHudLine getChatHudLine()
    {
        return chatHudLine;
    }

    public boolean getAnimationMode()
    {
        return animationMode;
    }
}

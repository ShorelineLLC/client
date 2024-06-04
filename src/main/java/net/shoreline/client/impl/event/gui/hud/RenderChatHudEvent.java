package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.client.impl.module.misc.BetterChatModule;
import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;

@Cancelable
public class RenderChatHudEvent extends Event
{
    private final ChatHudLine chatHudLine;
    private double animation;
    private BetterChatModule.AnimationMode animationMode;

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

    public void setAnimationMode(BetterChatModule.AnimationMode animationMode)
    {
        this.animationMode = animationMode;
    }

    public ChatHudLine getChatHudLine()
    {
        return chatHudLine;
    }

    public BetterChatModule.AnimationMode getAnimationMode()
    {
        return animationMode;
    }
}

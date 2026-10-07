package net.shoreline.client.impl.event.gui.hud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@AllArgsConstructor
@Cancelable
@Getter
@Setter
public class ChatMessageEvent extends Event
{
    private Text text;

    @RequiredArgsConstructor
    @Getter
    public static class Visible extends Event
    {
        private final ChatHudLine.Visible chatLine;
    }
}

package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.text.Text;
import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

@Cancelable
public class ChatMessageEvent extends Event {
    private Text text;

    public ChatMessageEvent(Text text) {
        this.text = text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Text getText() {
        return text;
    }
}

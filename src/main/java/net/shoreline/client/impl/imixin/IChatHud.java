package net.shoreline.client.impl.imixin;

import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;

@IMixin
public interface IChatHud
{

    void addMessage(Text message, MessageIndicator indicator, int id);
}

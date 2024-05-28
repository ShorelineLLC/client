package net.shoreline.client.impl.imixin;

import net.minecraft.text.Text;

@IMixin
public interface IChatHud {

    void addMessage(Text message, int id);
}

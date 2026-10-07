package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.client.impl.imixin.IChatHudLineVisible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class MixinChatHudLineVisible implements IChatHudLineVisible
{
    @Unique
    private int messageId;

    @Override
    public int getId()
    {
        return messageId;
    }

    @Override
    public void setId(int id)
    {
        this.messageId = id;
    }
}

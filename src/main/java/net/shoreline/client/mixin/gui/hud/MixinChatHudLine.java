package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.client.impl.imixin.IChatHudLine;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.class)
public abstract class MixinChatHudLine implements IChatHudLine, Globals
{
    @Unique
    private int id;

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }
}

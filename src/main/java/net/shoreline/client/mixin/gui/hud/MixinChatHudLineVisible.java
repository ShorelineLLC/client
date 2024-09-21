package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.OrderedText;
import net.shoreline.client.impl.event.gui.hud.ChatLineEvent;
import net.shoreline.client.impl.imixin.IChatHudLineVisible;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHudLine.Visible.class)
public class MixinChatHudLineVisible implements IChatHudLineVisible, Globals
{
    @Unique
    private int id;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void hookInit(int i, OrderedText orderedText, MessageIndicator messageIndicator, boolean bl, CallbackInfo ci)
    {
        ChatLineEvent chatLineEvent = new ChatLineEvent((ChatHudLine.Visible) (Object) this, -mc.textRenderer.getWidth(orderedText));
        EventBus.INSTANCE.dispatch(chatLineEvent);
    }

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

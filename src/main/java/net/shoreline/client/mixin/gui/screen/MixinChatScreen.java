package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.screen.ChatScreen;
import net.shoreline.client.impl.event.gui.screen.ChatScreenEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen
{
    @Inject(method = "sendMessage", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendMessage(String chatText, boolean addToHistory, CallbackInfo ci)
    {
        ChatScreenEvent.SendMessage chatScreenEvent = new ChatScreenEvent.SendMessage(chatText);
        EventBus.INSTANCE.dispatch(chatScreenEvent);
        if (chatScreenEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}

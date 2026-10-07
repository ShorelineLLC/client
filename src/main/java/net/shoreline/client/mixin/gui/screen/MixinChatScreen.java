package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.shoreline.client.impl.event.gui.hud.RenderChatEvent;
import net.shoreline.client.impl.event.gui.screen.ChatScreenEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends MixinScreen
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

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void hookRender(DrawContext instance, int x1, int y1, int x2, int y2, int color)
    {
        int y = (int) (this.height - 2.0f);
        RenderChatEvent.Background event = new RenderChatEvent.Background(instance, 2, y, this.width - 2, color);
        EventBus.INSTANCE.dispatch(event);
    }
}

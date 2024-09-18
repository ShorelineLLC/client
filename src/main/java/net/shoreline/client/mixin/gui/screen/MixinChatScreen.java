package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.shoreline.client.impl.event.gui.chat.ChatInputEvent;
import net.shoreline.client.impl.event.gui.chat.ChatKeyInputEvent;
import net.shoreline.client.impl.event.gui.chat.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.chat.ChatRenderEvent;
import net.shoreline.client.impl.event.gui.screen.RenderOpenChatEvent;
import net.shoreline.client.mixin.accessor.AccessorTextFieldWidget;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @see ChatScreen
 * @since 1.0
 */
@Mixin(ChatScreen.class)
public class MixinChatScreen extends MixinScreen
{
    @Shadow
    protected TextFieldWidget chatField;

    /**
     * @param chatText
     */
    @Inject(method = "onChatFieldUpdate", at = @At(value = "TAIL"))
    private void hookOnChatFieldUpdate(String chatText, CallbackInfo ci)
    {
        ChatInputEvent chatInputEvent = new ChatInputEvent(chatText);
        EventBus.INSTANCE.dispatch(chatInputEvent);
    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     * @param cir
     */
    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void hookKeyPressed(int keyCode, int scanCode, int modifiers,
                                CallbackInfoReturnable<Boolean> cir)
    {
        ChatKeyInputEvent keyInputEvent = new ChatKeyInputEvent(keyCode,
                chatField.getText());
        EventBus.INSTANCE.dispatch(keyInputEvent);
        if (keyInputEvent.isCanceled())
        {
            cir.cancel();
            chatField.setText(keyInputEvent.getChatText());
        }
    }

    /**
     * @param chatText
     * @param addToHistory
     */
    @Inject(method = "sendMessage", at = @At(value = "HEAD"), cancellable = true)
    private void hookSendMessage(String chatText, boolean addToHistory, CallbackInfo ci)
    {
        ChatMessageEvent.Client chatMessageEvent =
                new ChatMessageEvent.Client(chatText);
        EventBus.INSTANCE.dispatch(chatMessageEvent);
        if (chatMessageEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     * @param ci
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/" +
            "widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.BEFORE))
    private void hookRender(DrawContext context, int mouseX, int mouseY,
                            float delta, CallbackInfo ci)
    {
        float x = ((AccessorTextFieldWidget) chatField).isDrawsBackground() ?
                chatField.getX() + 6 : chatField.getX() + 2;
        float y = ((AccessorTextFieldWidget) chatField).isDrawsBackground() ?
                chatField.getY() + (chatField.getHeight() - 8) / 2.0f :
                chatField.getY();
        ChatRenderEvent chatTextRenderEvent = new ChatRenderEvent(context, x, y);
        EventBus.INSTANCE.dispatch(chatTextRenderEvent);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void hookFill(DrawContext instance, int x1, int y1, int x2, int y2, int color)
    {
        RenderOpenChatEvent renderOpenChatEvent = new RenderOpenChatEvent();
        EventBus.INSTANCE.dispatch(renderOpenChatEvent);
        float openAnimation = renderOpenChatEvent.isCanceled() ? 12.0f * renderOpenChatEvent.getAnimation() : 12.0f;
        instance.fill(2, (int) (this.height - 2.0f), this.width - 2, (int) ((this.height - 2.0f) - openAnimation), client.options.getTextBackgroundColor(Integer.MIN_VALUE));
    }

    @Override
    protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement)
    {
        return null;
    }
}

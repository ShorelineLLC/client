package net.shoreline.client.mixin.gui.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.event.gui.chat.ChatHistoryEvent;
import net.shoreline.client.impl.event.gui.chat.ChatLengthEvent;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.hud.ChatTextEvent;
import net.shoreline.client.impl.event.gui.hud.RenderChatHudEvent;
import net.shoreline.client.impl.event.gui.hud.SignatureIndicatorEvent;
import net.shoreline.client.impl.imixin.IChatHud;
import net.shoreline.client.impl.imixin.IChatHudLine;
import net.shoreline.client.impl.imixin.IChatHudLineVisible;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHud, Globals
{
    @Shadow
    @Final
    private List<ChatHudLine> messages;
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    public abstract void addMessage(Text message);

    @Shadow
    public abstract double getChatScale();

    @Shadow
    public abstract int getWidth();

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private boolean hasUnreadNewMessages;

    @Shadow
    public abstract void scroll(int scroll);

    @Shadow
    private int scrolledLines;

    @Shadow
    protected abstract boolean isChatFocused();

    private ChatHudLine current = null;
    private int currentId;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"))
    private void hookTimeAdded(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex)
    {
        try
        {
            current = messages.get(chatLineIndex);
        }
        catch (Exception ignored)
        {

        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;" +
                            "drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;" +
                            "Lnet/minecraft/text/OrderedText;III)I"))
    private int drawTextWithShadowHook(DrawContext instance,
                                       TextRenderer textRenderer,
                                       OrderedText text,
                                       int x,
                                       int y,
                                       int color)
    {
        RenderChatHudEvent renderChatHudEvent = new RenderChatHudEvent(current);
        EventBus.INSTANCE.dispatch(renderChatHudEvent);
        if (renderChatHudEvent.isCanceled())
        {
            if (renderChatHudEvent.getAnimationMode())
            {
                return instance.drawTextWithShadow(textRenderer, text, (int) renderChatHudEvent.getAnimation(), y, color);
            }
            else
            {
                float alpha = (float) MathHelper.clamp(renderChatHudEvent.getAnimation(), 0.0f, 1.0f);

                float colorAlpha = (color >> 24) & 0xFF;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
                int colorAlphaInt = Math.max(10, (int) (colorAlpha * alpha));
                return instance.drawTextWithShadow(textRenderer, text, 0, y, alpha == 1.0f ? color : (colorAlphaInt << 24) | (color & 0xFFFFFF));
            }
        }
        return instance.drawTextWithShadow(textRenderer, text, 0, y, color);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/" +
            "ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator hookRender(MessageIndicator original)
    {
        SignatureIndicatorEvent signatureIndicatorEvent = new SignatureIndicatorEvent();
        EventBus.INSTANCE.dispatch(signatureIndicatorEvent);
        return signatureIndicatorEvent.isCanceled() ? null : original;
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci)
    {
        ci.cancel();
        visibleMessages.removeIf(msg -> ((IChatHudLineVisible) (Object) msg).getId() == currentId && currentId != 0);
        for (int i = messages.size() - 1; i > -1; i--)
        {
            if (((IChatHudLine) (Object) messages.get(i)).getId() == currentId && currentId != 0)
            {
                messages.remove(i);
            }
        }
        ChatMessageEvent chatTextEvent = new ChatMessageEvent(message);
        EventBus.INSTANCE.dispatch(chatTextEvent);
        int i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
        if (indicator != null && indicator.icon() != null)
        {
            i -= indicator.icon().width + 4 + 2;
        }
        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(chatTextEvent.isCanceled() ? chatTextEvent.getText() : message, i, this.client.textRenderer);
        boolean bl = this.isChatFocused();
        for (int j = 0; j < list.size(); ++j)
        {
            OrderedText orderedText = list.get(j);
            if (bl && this.scrolledLines > 0)
            {
                this.hasUnreadNewMessages = true;
                this.scroll(1);
            }
            ChatTextEvent chatMessageEvent = new ChatTextEvent(orderedText);
            EventBus.INSTANCE.dispatch(chatMessageEvent);
            boolean bl2 = j == list.size() - 1;
            ChatHudLine.Visible visibleLine = new ChatHudLine.Visible(mc.inGameHud.getTicks(), chatMessageEvent.isCanceled() ? chatMessageEvent.getText() : orderedText, indicator, bl2);
            ((IChatHudLineVisible) (Object) visibleLine).setId(currentId);
            this.visibleMessages.add(0, visibleLine);
        }
        ChatLengthEvent chatLengthEvent = new ChatLengthEvent();
        EventBus.INSTANCE.dispatch(chatLengthEvent);
        boolean bl1 = chatLengthEvent.isCanceled();
        if (!bl1)
        {
            while (this.visibleMessages.size() > 100)
            {
                this.visibleMessages.remove(this.visibleMessages.size() - 1);
            }
        }
    }

    @Inject(method = "clear", at = @At(value = "HEAD"), cancellable = true)
    private void hookClear(boolean clearHistory, CallbackInfo ci)
    {
        ChatHistoryEvent chatHistoryEvent = new ChatHistoryEvent();
        EventBus.INSTANCE.dispatch(chatHistoryEvent);
        if (chatHistoryEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Override
    public void addMessage(Text message, int id)
    {
        currentId = id;
        addMessage(message);
        currentId = 0;
    }
}

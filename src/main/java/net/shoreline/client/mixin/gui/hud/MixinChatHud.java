package net.shoreline.client.mixin.gui.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.impl.event.gui.hud.MessageIndicatorEvent;
import net.shoreline.client.impl.event.gui.hud.RenderChatEvent;
import net.shoreline.client.impl.imixin.IChatHud;
import net.shoreline.client.impl.imixin.IChatHudLine;
import net.shoreline.client.impl.imixin.IChatHudLineVisible;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHud
{
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Shadow
    public abstract void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator);

    @Unique
    private ChatHudLine.Visible currentLine;

    @Unique
    private int messageId;

    @Override
    public void addMessage(Text message, MessageIndicator messageIndicator, int id)
    {
        messageId = id;
        addMessage(message, null, messageIndicator);
        messageId = 0;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"))
    private void hookTimeAdded(CallbackInfo ci,
                               @Local(ordinal = 13) int chatLineIndex)
    {
        try
        {
            currentLine = visibleMessages.get(chatLineIndex);
        } catch (Exception ignored)
        {

        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    private int hookDrawText(DrawContext instance,
                             TextRenderer textRenderer,
                             OrderedText text,
                             int x,
                             int y,
                             int color,
                             @Local(name = "u") int u)
    {
        RenderChatEvent.Text renderChatTextEvent = new RenderChatEvent.Text(currentLine, instance, text, x, y, color, u);
        EventBus.INSTANCE.dispatch(renderChatTextEvent);
        return renderChatTextEvent.isCanceled() ? 0 : instance.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    @Redirect(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At(value = "NEW",
                    target = "(ILnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)Lnet/minecraft/client/gui/hud/ChatHudLine;")
    )
    private ChatHudLine hookInitChatLine(int creationTick,
                                         Text text,
                                         MessageSignatureData messageSignatureData,
                                         MessageIndicator messageIndicator)
    {
        ChatMessageEvent chatMessageEvent = new ChatMessageEvent(text);
        EventBus.INSTANCE.dispatch(chatMessageEvent);
        if (chatMessageEvent.isCanceled())
        {
            text = chatMessageEvent.getText();
        }

        return new ChatHudLine(creationTick, text, messageSignatureData, messageIndicator);
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At(value = "HEAD")
    )
    private void hookAddMessage(Text message,
                                MessageSignatureData signatureData,
                                MessageIndicator indicator,
                                CallbackInfo ci)
    {
        if (messageId == 0)
        {
            return;
        }

        visibleMessages.removeIf(msg -> ((IChatHudLineVisible) (Object) msg).getId() == messageId);
        for (int i = messages.size() - 1; i > -1; i--)
        {
            if (((IChatHudLine) (Object) messages.get(i)).getId() == messageId)
            {
                messages.remove(i);
            }
        }
    }

    @Redirect(
            method = "addVisibleMessage",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    private void hookAddVisibleMessage(List instance, int i, Object e)
    {
        ChatHudLine.Visible chatLine = (ChatHudLine.Visible) e;
        ChatMessageEvent.Visible event = new ChatMessageEvent.Visible(chatLine);
        EventBus.INSTANCE.dispatch(event);

        ((IChatHudLineVisible) e).setId(messageId);
        instance.add(0, e);
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator hookRender(MessageIndicator original)
    {
        MessageIndicatorEvent signatureIndicatorEvent = new MessageIndicatorEvent();
        EventBus.INSTANCE.dispatch(signatureIndicatorEvent);
        return signatureIndicatorEvent.isCanceled() ? null : original;
    }
}

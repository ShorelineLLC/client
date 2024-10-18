package net.shoreline.client.mixin.gui.screen;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.shoreline.client.impl.event.gui.screen.SuggestChatEvent;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggestor
{

    @Shadow
    private ParseResults<CommandSource> parse;

    @Shadow
    @Final
    private TextFieldWidget textField;

    @Shadow
    private boolean completingSuggestions;

    @Shadow
    @Nullable
    private ChatInputSuggestor.@Nullable SuggestionWindow window;

    @Shadow
    private @Nullable CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected abstract void showCommandSuggestions();

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/" +
            "StringReader;canRead()Z", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void hookRefresh(CallbackInfo ci, String string, StringReader stringReader)
    {
        SuggestChatEvent suggestChatEvent = new SuggestChatEvent();
        EventBus.INSTANCE.dispatch(suggestChatEvent);
        if (stringReader.getString().startsWith(suggestChatEvent.getPrefix(), stringReader.getCursor()))
        {
            stringReader.setCursor(stringReader.getCursor() + 1);
            if (parse == null)
            {
                parse = suggestChatEvent.getDispatcher().parse(stringReader, suggestChatEvent.getSource());
            }
            int cursor = textField.getCursor();
            if (cursor >= 1 && (window == null || !completingSuggestions))
            {
                pendingSuggestions = suggestChatEvent.getDispatcher().getCompletionSuggestions(parse, cursor);
                pendingSuggestions.thenRun(() ->
                {
                    if (pendingSuggestions.isDone())
                    {
                        showCommandSuggestions();
                    }
                });
            }
            ci.cancel();
        }
    }
}

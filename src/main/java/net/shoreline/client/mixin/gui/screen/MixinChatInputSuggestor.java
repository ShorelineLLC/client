package net.shoreline.client.mixin.gui.screen;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.command.CommandManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggestor
{
    @Shadow
    private ParseResults<CommandSource> parse;

    @Shadow
    @Final
    TextFieldWidget textField;

    @Shadow
    boolean completingSuggestions;

    @Shadow
    private ChatInputSuggestor.SuggestionWindow window;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected abstract void showCommandSuggestions();

    @Inject(method = "refresh",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false), cancellable = true)
    private void hookRefresh(CallbackInfo ci, @Local StringReader stringReader)
    {
        CommandManager commandManager = Managers.COMMANDS;
        if (stringReader.getString().startsWith(commandManager.getChatPrefix(), stringReader.getCursor()))
        {
            stringReader.setCursor(stringReader.getCursor() + 1);
            if (parse == null)
            {
                parse = commandManager.getDispatcher().parse(stringReader, commandManager.getSource());
            }
            int cursor = textField.getCursor();
            if (cursor >= 1 && (window == null || !completingSuggestions))
            {
                pendingSuggestions = commandManager.getDispatcher().getCompletionSuggestions(parse, cursor);
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

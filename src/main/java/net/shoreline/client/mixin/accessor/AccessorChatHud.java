package net.shoreline.client.mixin.accessor;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatHud.class)
public interface AccessorChatHud
{

    @Invoker("addMessage")
    void hookAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator);
}

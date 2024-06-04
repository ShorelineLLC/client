package net.shoreline.client.mixin.accessor;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ChatScreen.class)
public interface AccessorChatScreen
{
    /**
     * @return
     * @see ChatScreen#chatField
     */
    @Accessor("chatField")
    TextFieldWidget getChatField();
}

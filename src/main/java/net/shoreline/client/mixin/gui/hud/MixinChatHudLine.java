package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.gui.hud.ChatLineEvent;
import net.shoreline.client.impl.event.handler.EventBus;
import net.shoreline.client.impl.imixin.IChatHudLine;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.render.animation.TimeAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHudLine.class)
public abstract class MixinChatHudLine implements IChatHudLine, Globals
{
    @Unique
    private int id;

    /**
     * Change "Modules.BETTER_CHAT.getEasingConfig())" to
     * Modules.BETTER_CHAT.getEasingConfig().getValue())
     * when linus fixes enumconfig!
     */
    @Inject(
            method = "<init>",
            at = @At(value = "RETURN"))
    private void hookCtr(int creationTick,
                         Text text,
                         MessageSignatureData messageSignatureData,
                         MessageIndicator messageIndicator,
                         CallbackInfo info)
    {
        ChatLineEvent chatLineEvent = new ChatLineEvent(ChatHudLine.class.cast(this), -mc.textRenderer.getWidth(text.getString()));
        EventBus.EVENT_HANDLER.dispatch(chatLineEvent);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
}

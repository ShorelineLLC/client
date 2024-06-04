package net.shoreline.client.mixin.accessor;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 *
 */
@Mixin(ClickableWidget.class)
public interface AccessorClickableWidget
{
    /**
     * @param message
     */
    @Accessor("message")
    void setMessage(Text message);
}

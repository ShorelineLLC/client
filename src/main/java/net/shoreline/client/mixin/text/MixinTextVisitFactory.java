package net.shoreline.client.mixin.text;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import net.shoreline.client.Shoreline;
import net.shoreline.client.impl.event.text.TextVisitedEvent;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory
{
    @ModifyArg(
            method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
                    ordinal = 0
            ),
            index = 0)
    private static String hookVisitFormatted(String text)
    {
        if (text == null)
        {
            return "";
        }

        TextVisitedEvent textVisitEvent = new TextVisitedEvent(text);
        EventBus.INSTANCE.dispatch(textVisitEvent);
        return textVisitEvent.isCanceled() ? textVisitEvent.getText() : text;
    }

    @Redirect(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Formatting;byCode(C)Lnet/minecraft/util/Formatting;"
            )
    )
    private static Formatting hookVisitFormatted$2(char code)
    {
        return code == 'j' || code == 'g' || code == 'h' ? Formatting.WHITE : Formatting.byCode(code);
    }

    @Redirect(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Style;withExclusiveFormatting(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/Style;"
            )
    )
    private static Style hookVisitFormatted(Style instance,
                                            Formatting formatting,
                                            @Local(name = "d") char d,
                                            @Local(name = "j") LocalIntRef iRef,
                                            @Local(argsOnly = true) String text)
    {
        if (d == 'g')
        {
            return instance.withColor(ThemeModule.INSTANCE.getPrimaryColor().getRGB());
        } else if (d == 'h')
        {
            return instance.withColor(SocialsModule.INSTANCE.getFriendsColor().getRGB());
        }
        else if (d == 'j')
        {
            int start  = iRef.get() + 2;
            int end    = Math.min(start + 8, text.length());
            String hex = text.substring(start, end);
            try
            {
                int argb = (int) Long.parseLong(hex, 16);
                iRef.set(end - 2);
                return instance.withColor(argb);
            }
            catch (NumberFormatException ignored)
            {
            }
        }

        return instance.withExclusiveFormatting(formatting);
    }
}

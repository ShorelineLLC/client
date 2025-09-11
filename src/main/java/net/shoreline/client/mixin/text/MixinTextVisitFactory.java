package net.shoreline.client.mixin.text;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory
{
    @Redirect(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Formatting;byCode(C)Lnet/minecraft/util/Formatting;"
            )
    )
    private static Formatting hookVisitFormatted$2(char code)
    {
        return code == 'g' || code == 'h' ? Formatting.WHITE : Formatting.byCode(code);
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
                                            @Local(name = "d") char d)
    {
        if (d == 'g')
        {
            return instance.withColor(ThemeModule.INSTANCE.getPrimaryColor().getRGB());
        } else if (d == 'h')
        {
            return instance.withColor(SocialsModule.INSTANCE.getFriendsColor().getRGB());
        }

        return instance.withExclusiveFormatting(formatting);
    }
}

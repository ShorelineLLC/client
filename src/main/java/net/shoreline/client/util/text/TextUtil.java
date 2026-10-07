package net.shoreline.client.util.text;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@UtilityClass
public class TextUtil 
{
    final Map<Integer, Formatting> COLOR_TO_FORMATTING = Stream.of(Formatting.values())
            .filter(Formatting::isColor)
            .collect(ImmutableMap.toImmutableMap(Formatting::getColorValue, Function.identity()));

    public String parseString(OrderedText text)
    {
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) ->
        {
            if (style.getColor() != null)
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX);
                int rgb = style.getColor().getRgb();
                
                int clientColor = ThemeModule.INSTANCE.getPrimaryColor().getRGB();
                int friendsColor = SocialsModule.INSTANCE.getFriendsColor().getRGB();
                
                if (rgb == (clientColor & 0xFFFFFF))
                {
                    builder.append("g");
                } else if (rgb == (friendsColor & 0xFFFFFF))
                {
                    builder.append("h");
                } else
                {
                    Formatting formatting = COLOR_TO_FORMATTING.get(rgb);
                    if (formatting != null)
                    {
                        builder.append(formatting.getCode());
                    }
                }
            } else if (style.isObfuscated())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("k");
            } else if (style.isBold())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("l");
            } else if (style.isStrikethrough())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("m");
            } else if (style.isUnderlined())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("n");
            } else if (style.isItalic())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("o");
            } else
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("r");
            }

            builder.appendCodePoint(codePoint);
            return true;
        });

        return builder.toString();
    }
}

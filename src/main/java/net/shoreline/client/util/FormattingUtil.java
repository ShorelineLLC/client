package net.shoreline.client.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.client.SocialsModule;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class FormattingUtil
{
    private static final Map<Integer, Formatting> COLOR_TO_FORMATTING = Stream.of(Formatting.values()).filter(Formatting::isColor)
            .collect(ImmutableMap.toImmutableMap(Formatting::getColorValue, Function.identity()));

    // Fuck minecraft
    public static String toString(Text text)
    {
        StringBuilder builder = new StringBuilder();
        text.visit((styleOverride, message) ->
        {
            if (!message.isEmpty())
            {
                if (styleOverride.getColor() != null)
                {
                    int rgb = styleOverride.getColor().getRgb();
                    if (rgb == (ColorsModule.getInstance().getRGB() & 0xFFFFFF))
                    {
                        builder.append(Formatting.FORMATTING_CODE_PREFIX).append("s");
                    }
                    else if (rgb == (SocialsModule.getInstance().getFriendRGB() & 0xFFFFFF))
                    {
                        builder.append(Formatting.FORMATTING_CODE_PREFIX).append("g");
                    }
                    else
                    {
                        Formatting formatting = COLOR_TO_FORMATTING.get(rgb);
                        if (formatting != null)
                        {
                            builder.append(Formatting.FORMATTING_CODE_PREFIX).append(formatting.getCode());
                        }
                    }
                }
                else if (styleOverride.isObfuscated())
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("k");
                }
                else if (styleOverride.isBold())
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("l");
                }
                else if (styleOverride.isStrikethrough())
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("m");
                }
                else if (styleOverride.isUnderlined())
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("n");
                }
                else if (styleOverride.isItalic())
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("o");
                }
                else
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("r");
                }
                builder.append(message);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return builder.toString();
    }

    public static String toString(OrderedText text)
    {
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) ->
        {
            if (style.getColor() != null)
            {
                int rgb = style.getColor().getRgb();
                if (rgb == (ColorsModule.getInstance().getRGB() & 0xFFFFFF))
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("s");
                }
                else if (rgb == (SocialsModule.getInstance().getFriendRGB() & 0xFFFFFF))
                {
                    builder.append(Formatting.FORMATTING_CODE_PREFIX).append("g");
                }
                else
                {
                    Formatting formatting = COLOR_TO_FORMATTING.get(rgb);
                    if (formatting != null)
                    {
                        builder.append(Formatting.FORMATTING_CODE_PREFIX).append(formatting.getCode());
                    }
                }
            }
            else if (style.isObfuscated())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("k");
            }
            else if (style.isBold())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("l");
            }
            else if (style.isStrikethrough())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("m");
            }
            else if (style.isUnderlined())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("n");
            }
            else if (style.isItalic())
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("o");
            }
            else
            {
                builder.append(Formatting.FORMATTING_CODE_PREFIX).append("r");
            }
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}

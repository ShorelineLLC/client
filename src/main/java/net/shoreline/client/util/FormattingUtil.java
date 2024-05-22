package net.shoreline.client.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class FormattingUtil {

    private static final Map<Integer, Formatting> COLOR_TO_FORMATTING = Stream.of(Formatting.values()).filter(Formatting::isColor).collect(ImmutableMap.toImmutableMap(formatting -> formatting.getColorValue(), Function.identity()));

    // Fuck minecraft
    public static String toString(Text text) {
        StringBuilder builder = new StringBuilder();
        text.visit((styleOverride, message) -> {
            if (!message.isEmpty()) {
                if (styleOverride.getColor() != null) {
                    Formatting formatting = COLOR_TO_FORMATTING.get(styleOverride.getColor().getRgb());
                    if (formatting != null) {
                        builder.append(Formatting.FORMATTING_CODE_PREFIX).append(formatting.getCode());
                    }
                }
                builder.append(message);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return builder.toString();
    }
}

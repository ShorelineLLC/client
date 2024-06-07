package net.shoreline.client.util.string;

/**
 * @author linus
 * @since 1.0
 */
public class StringUtil {
    /**
     * Capitalises a given string
     *
     * @param string The string to capitalise
     * @return The string with the first letter capitalised
     */
    public static String capitalize(final String string) {
        if (string.length() != 0) {
            return Character.toTitleCase(string.charAt(0)) + string.substring(1);
        }
        return "";
    }
}

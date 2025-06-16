package net.shoreline.client.util;

public class Formatter
{
    public static String formatEnum(final Enum<?> in)
    {
        String name = in.name();
        // no capitalization
        if (!name.contains("_"))
        {
            char firstChar = name.charAt(0);
            String suffixChars = name.split(String.valueOf(firstChar), 2)[1];
            return String.valueOf(firstChar).toUpperCase() + suffixChars.toLowerCase();
        }
        String[] names = name.split("_");
        StringBuilder nameToReturn = new StringBuilder();
        for (String n : names)
        {
            char firstChar = n.charAt(0);
            String suffixChars = n.split(String.valueOf(firstChar), 2)[1];
            nameToReturn.append(String.valueOf(firstChar).toUpperCase())
                    .append(suffixChars.toLowerCase());
        }
        return nameToReturn.toString();
    }
}

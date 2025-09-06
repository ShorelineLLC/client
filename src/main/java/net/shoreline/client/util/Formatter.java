package net.shoreline.client.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Formatter
{
    public String formatEnum(final Enum<?> in)
    {
        String name = in.name();
        if (name.equalsIgnoreCase("KMH"))
        {
            return "KMH";
        } else if (name.equalsIgnoreCase("BPS"))
        {
            return "BPS";
        } else if (name.equalsIgnoreCase("NCP"))
        {
            return "NCP";
        }

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
